package com.example.habittracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TodayFragment extends Fragment implements RefreshListener {
    private static final String ARG_DATE = "date";
    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private String currentDate;
    private Uri cameraImageUri;
    private String cameraImagePath;
    private TextView todayText;
    private TextView dateText;
    private TextView dayMonday;
    private TextView dayTuesday;
    private TextView dayWednesday;
    private TextView dayThursday;
    private TextView dayFriday;
    private TextView daySaturday;
    private TextView daySunday;
    private RecyclerView habitsRecyclerView;
    private RecyclerView todosRecyclerView;
    private RecyclerView eventsRecyclerView;
    private RecyclerView journalsRecyclerView;
    private RecyclerView alarmsRecyclerView;
    private HabitTrackerDatabase database;
    private ImageButton btnMood;
    private ImageButton btnAlarm;
    private ImageButton btnCalendar;
    private ImageButton menuButton;
    private ImageButton btnBackWeekly;
    private MaterialCardView photoCard;
    private ImageView photoPreview;
    private TextView photoPlaceholder;
    private LinearLayout photoPlaceholderContainer;
    private MaterialCardView todoPlaceholderCard;
    private MaterialCardView eventPlaceholderCard;
    private MaterialCardView journalPlaceholderCard;
    private LinearLayout dayNavigationBoxes;

    private String[] moods = {"Very Sad", "Sad", "Neutral", "Happy", "Very Happy"};
    private String[] moodEmojis = {"😢", "😞", "😐", "😊", "😎"};

    public static TodayFragment newInstance(String date) {
        TodayFragment fragment = new TodayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentDate = getArguments().getString(ARG_DATE);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            currentDate = sdf.format(Calendar.getInstance().getTime());
        }
        database = HabitTrackerDatabase.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_today, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        android.util.Log.d("TodayFragment", "onViewCreated for " + currentDate + " instance " + hashCode());

        // Apply theme background
        // Theme is now handled by Material3 DayNight

        todayText = view.findViewById(R.id.today_text);
        dateText = view.findViewById(R.id.date_text);
        dayMonday = view.findViewById(R.id.day_monday);
        dayTuesday = view.findViewById(R.id.day_tuesday);
        dayWednesday = view.findViewById(R.id.day_wednesday);
        dayThursday = view.findViewById(R.id.day_thursday);
        dayFriday = view.findViewById(R.id.day_friday);
        daySaturday = view.findViewById(R.id.day_saturday);
        daySunday = view.findViewById(R.id.day_sunday);
        habitsRecyclerView = view.findViewById(R.id.habits_recycler_view);
        todosRecyclerView = view.findViewById(R.id.todos_recycler_view);
        eventsRecyclerView = view.findViewById(R.id.events_recycler_view);
        journalsRecyclerView = view.findViewById(R.id.journals_recycler_view);
        alarmsRecyclerView = view.findViewById(R.id.alarms_recycler_view);
        btnMood = view.findViewById(R.id.btn_mood);
        btnAlarm = view.findViewById(R.id.btn_alarm);
        btnCalendar = view.findViewById(R.id.btn_calendar);
        menuButton = view.findViewById(R.id.menu_button);
        btnBackWeekly = view.findViewById(R.id.btn_back_weekly);
        photoCard = view.findViewById(R.id.photo_card);
        photoPreview = view.findViewById(R.id.photo_preview);
        photoPlaceholder = view.findViewById(R.id.photo_placeholder);
        photoPlaceholderContainer = view.findViewById(R.id.photo_placeholder_container);
        todoPlaceholderCard = view.findViewById(R.id.todo_placeholder_card);
        eventPlaceholderCard = view.findViewById(R.id.event_placeholder_card);
        journalPlaceholderCard = view.findViewById(R.id.journal_placeholder_card);
        dayNavigationBoxes = view.findViewById(R.id.day_navigation_boxes);

        setupDateDisplay();
        setupDayNavigation();
        setupMoodSlider();
        setupHabitsList();
        setupTodosList();
        setupEventsList();
        setupJournalsList();
        setupAlarmsList();
        setupPhotoCard();
        setupButtons();
        setupAlarmButton();

        loadMoodForDate();
        loadPhoto();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Apply theme background in case it changed
        // Refresh all data when fragment becomes visible
        onRefresh();
    }

    @Override
    public void onRefresh() {
        if (getView() == null || !isAdded()) {
            android.util.Log.w("TodayFragment", "onRefresh skipped: view is null or fragment not added for " + currentDate);
            return;
        }
        android.util.Log.d("TodayFragment", "onRefresh started for " + currentDate + " instance " + hashCode());
        try {
            loadHabits();
            loadTodos();
            loadEvents();
            loadJournals();
            loadAlarms();
            loadPhoto();
            loadMoodForDate();
        } catch (Exception e) {
            android.util.Log.e("TodayFragment", "Error during onRefresh for " + currentDate, e);
        }
        android.util.Log.d("TodayFragment", "onRefresh completed for " + currentDate + " instance " + hashCode());
    }

    private void setupDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(currentDate));

            SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, d MMMM", Locale.ENGLISH);
            dateText.setText(displayFormat.format(cal.getTime()));

            Calendar today = Calendar.getInstance();
            if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                todayText.setText("TODAY");
            } else {
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);
                todayText.setText(dayFormat.format(cal.getTime()).toUpperCase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupDayNavigation() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        try {
            Calendar currentCal = Calendar.getInstance();
            currentCal.setTime(sdf.parse(currentDate));

            int dayOfWeek = currentCal.get(Calendar.DAY_OF_WEEK);
            int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
            Calendar weekStart = (Calendar) currentCal.clone();
            weekStart.add(Calendar.DAY_OF_YEAR, -daysFromMonday);

            TextView[] dayButtons = {dayMonday, dayTuesday, dayWednesday, dayThursday, dayFriday, daySaturday, daySunday};

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            for (int i = 0; i < 7; i++) {
                Calendar dayCal = (Calendar) weekStart.clone();
                dayCal.add(Calendar.DAY_OF_YEAR, i);

                long diff = dayCal.getTimeInMillis() - currentCal.getTimeInMillis();
                final int dayOffset = (int) (diff / (1000 * 60 * 60 * 24));

                boolean isCurrentDay = dayCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                      dayCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);

                boolean isDisplayedDate = dayCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) &&
                                         dayCal.get(Calendar.DAY_OF_YEAR) == currentCal.get(Calendar.DAY_OF_YEAR);

                if (isCurrentDay) {
                    dayButtons[i].setBackgroundResource(R.drawable.day_button_today);
                    dayButtons[i].setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
                } else if (isDisplayedDate) {
                    dayButtons[i].setBackgroundResource(R.drawable.day_button_selected);
                    dayButtons[i].setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
                } else {
                    dayButtons[i].setBackgroundResource(R.drawable.rounded_card);
                    dayButtons[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
                }

                dayButtons[i].setOnClickListener(v -> navigateToDay(dayOffset));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToDay(int offset) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(currentDate));
            cal.add(Calendar.DAY_OF_YEAR, offset);
            String newDate = sdf.format(cal.getTime());

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToDate(newDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupMoodSlider() {
        btnMood.setOnClickListener(v -> {
            MoodDialog dialog = new MoodDialog();
            android.database.Cursor cursor = null;
            try {
                cursor = database.getMoodForDate(currentDate);
                if (cursor != null && cursor.moveToFirst()) {
                    int moodIdx = cursor.getColumnIndex("mood_type");
                    if (moodIdx != -1) {
                        String currentMood = cursor.getString(moodIdx);
                        dialog.setInitialMood(currentMood);
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("TodayFragment", "Error in setupMoodSlider", e);
            } finally {
                if (cursor != null) cursor.close();
            }

            dialog.setMoodSelectedListener((mood, feelings) -> {
                String feelingTags = String.join(",", feelings);
                saveMood(mood, feelingTags);
                updateMoodButton(mood);
            });
            dialog.show(getParentFragmentManager(), "mood_dialog");
        });
    }

    private void updateMoodButton(String mood) {
        // Map moods to colors or tint the icon
        int colorRes = R.color.text_primary; // Default
        
        // For now, we keep the static icon as requested (minimal/supportive).
        // If we want to show state, we could tint the icon or background.
        // Let's just unset any stuck state for now or keep it simple.
        
        // Example: If a mood is set, maybe tint the background slightly differently?
        // But the user requested "minimal detail".
        // So we will just leave the static icon.
    }

    private void setupHabitsList() {
        habitsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        loadHabits();
    }

    private void setupTodosList() {
        todosRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        loadTodos();
        
        // Setup drag and drop for todos
        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                0
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPos = viewHolder.getAdapterPosition();
                int toPos = target.getAdapterPosition();
                
                if (fromPos == RecyclerView.NO_POSITION || toPos == RecyclerView.NO_POSITION) {
                    return false;
                }
                
                TodoAdapter adapter = (TodoAdapter) recyclerView.getAdapter();
                if (adapter != null && adapter.todos != null) {
                    // Move item in list
                    if (fromPos < toPos) {
                        // Moving down
                        for (int i = fromPos; i < toPos; i++) {
                            java.util.Collections.swap(adapter.todos, i, i + 1);
                        }
                    } else {
                        // Moving up
                        for (int i = fromPos; i > toPos; i--) {
                            java.util.Collections.swap(adapter.todos, i, i - 1);
                        }
                    }
                    
                    adapter.notifyItemMoved(fromPos, toPos);
                    
                    // Update order in database
                    List<Long> todoIds = new ArrayList<>();
                    for (TodoItem todo : adapter.todos) {
                        todoIds.add(todo.id);
                    }
                    database.updateTodoOrder(todoIds);
                    
                    return true;
                }
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Not used for drag and drop
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true; // Enable long press to start dragging
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    // Change appearance when dragging starts
                    if (viewHolder != null) {
                        viewHolder.itemView.setAlpha(0.7f);
                        viewHolder.itemView.setScaleX(1.05f);
                        viewHolder.itemView.setScaleY(1.05f);
                    }
                }
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                // Restore appearance when dragging ends
                viewHolder.itemView.setAlpha(1.0f);
                viewHolder.itemView.setScaleX(1.0f);
                viewHolder.itemView.setScaleY(1.0f);
            }
        };
        
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(todosRecyclerView);
    }

    private void setupEventsList() {
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        loadEvents();
    }

    private void setupJournalsList() {
        journalsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        loadJournals();
    }

    private void setupAlarmsList() {
        alarmsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        loadAlarms();
    }

    private void setupAlarmButton() {
        btnAlarm.setOnClickListener(v -> {
            AddAlarmDialog dialog = new AddAlarmDialog();
            dialog.setDate(currentDate);
            dialog.setRefreshListener(this);
            dialog.show(getParentFragmentManager(), "add_alarm");
        });
    }

    private void cancelAlarm(long alarmId) {
        AlarmHelper.cancelAlarm(requireContext(), alarmId);
    }

    private void setupPhotoCard() {
        photoCard.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Add Photo")
                    .setItems(new String[]{"Galeriden Seç", "Kamera ile Çek"}, (dialog, which) -> {
                        if (which == 0) {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, REQUEST_IMAGE_PICK);
                        } else if (which == 1) {
                            checkCameraPermissionAndOpen();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void checkCameraPermissionAndOpen() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                return;
            }
        }
        openCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(requireContext(), "Kamera izni gerekli", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        try {
            File imagesDir = new File(requireContext().getFilesDir(), "images");
            if (!imagesDir.exists()) {
                if (!imagesDir.mkdirs()) {
                    Toast.makeText(requireContext(), "Klasör oluşturulamadı", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            File imageFile = new File(imagesDir, "photo_" + currentDate + "_" + System.currentTimeMillis() + ".jpg");
            cameraImagePath = imageFile.getAbsolutePath();

            cameraImageUri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                imageFile
            );

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            List<android.content.pm.ResolveInfo> cameraActivities = requireContext().getPackageManager()
                    .queryIntentActivities(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY);
            for (android.content.pm.ResolveInfo activity : cameraActivities) {
                requireContext().grantUriPermission(activity.activityInfo.packageName, cameraImageUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(requireContext(), "Kamera uygulaması bulunamadı", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Kamera açılamadı: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadHabits() {
        android.database.Cursor cursor = null;
        try {
            cursor = database.getHabitsForDate(currentDate);
            List<HabitItem> habits = new ArrayList<>();
            java.util.Set<Long> seenHabitIds = new java.util.HashSet<>();

            // Get current day of week (0=Sunday, 1=Monday, ..., 6=Saturday)
            Calendar calendar = Calendar.getInstance();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                calendar.setTime(sdf.parse(currentDate));
            } catch (Exception e) {
                calendar = Calendar.getInstance();
            }
            int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            // Convert to our format: 0=Sunday, 1=Monday, ..., 6=Saturday
            int dayIndex = (currentDayOfWeek == Calendar.SUNDAY) ? 0 : currentDayOfWeek - 1;

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int idIdx = cursor.getColumnIndex("id");
                    int nameIdx = cursor.getColumnIndex("name");
                    int catIdx = cursor.getColumnIndex("category");
                    int iconIdx = cursor.getColumnIndex("icon");
                    int compIdx = cursor.getColumnIndex("completed");
                    int repeatTypeIdx = cursor.getColumnIndex("repeat_type");
                    int daysOfWeekIdx = cursor.getColumnIndex("days_of_week");
                    int createdIdx = cursor.getColumnIndex("created_date");

                    if (idIdx == -1 || nameIdx == -1) continue;

                    long habitId = cursor.getLong(idIdx);

                    if (seenHabitIds.contains(habitId)) {
                        continue;
                    }
                    seenHabitIds.add(habitId);

                    // Check if habit should be shown today based on repeat type and days
                    String repeatType = repeatTypeIdx != -1 ? cursor.getString(repeatTypeIdx) : "Daily";
                    String daysOfWeek = daysOfWeekIdx != -1 ? cursor.getString(daysOfWeekIdx) : "";
                    
                    // For Weekly habits, check if today is in the selected days
                    if ("Weekly".equals(repeatType) && daysOfWeek != null && !daysOfWeek.isEmpty()) {
                        List<String> selectedDays = Arrays.asList(daysOfWeek.split(","));
                        if (!selectedDays.contains(String.valueOf(dayIndex))) {
                            // Today is not in the selected days, skip this habit
                            continue;
                        }
                    }
                    // For Daily habits, always show
                    // For Monthly habits, always show (can be enhanced later)

                    String name = cursor.getString(nameIdx);
                    String category = catIdx != -1 ? cursor.getString(catIdx) : "Custom";
                    String icon = iconIdx != -1 ? cursor.getString(iconIdx) : "📚";
                    int completed = compIdx != -1 ? cursor.getInt(compIdx) : 0;
                    long createdDateMs = 0;
                    if (createdIdx != -1) {
                        createdDateMs = parseCreatedDateValue(cursor.getString(createdIdx));
                    }
                    // Hide habits on days before they were created
                    long entryDayStart = parseDateToStartOfDay(currentDate);
                    if (createdDateMs > 0 && entryDayStart > 0 && entryDayStart < startOfDay(createdDateMs)) {
                        continue;
                    }

                    habits.add(new HabitItem(habitId, name, category, icon, completed == 1, createdDateMs));
                }
            }

            HabitAdapter adapter = (HabitAdapter) habitsRecyclerView.getAdapter();
            if (adapter == null) {
                adapter = new HabitAdapter(habits);
                habitsRecyclerView.setAdapter(adapter);
            } else {
                adapter.habits = habits;
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void loadTodos() {
        android.database.Cursor cursor = null;
        try {
            cursor = database.getTodosForDate(currentDate);
            List<TodoItem> todos = new ArrayList<>();

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int idIdx = cursor.getColumnIndex("id");
                    int titleIdx = cursor.getColumnIndex("title");
                    int descIdx = cursor.getColumnIndex("description");
                    int completedIdx = cursor.getColumnIndex("completed");
                    int priorityIdx = cursor.getColumnIndex("priority");

                    if (idIdx == -1 || titleIdx == -1) continue;

                    long id = cursor.getLong(idIdx);
                    String title = cursor.getString(titleIdx);
                    String description = descIdx != -1 ? cursor.getString(descIdx) : "";
                    int completed = completedIdx != -1 ? cursor.getInt(completedIdx) : 0;
                    int priority = priorityIdx != -1 ? cursor.getInt(priorityIdx) : 0;

                    // Only add active (uncompleted) todos
                    if (completed == 0) {
                        todos.add(new TodoItem(id, title, description, priority, false));
                    }
                }
            }

            if (todos.isEmpty()) {
                todosRecyclerView.setVisibility(View.GONE);
            } else {
                todosRecyclerView.setVisibility(View.VISIBLE);
                TodoAdapter adapter = (TodoAdapter) todosRecyclerView.getAdapter();
                if (adapter == null) {
                    adapter = new TodoAdapter(todos);
                    todosRecyclerView.setAdapter(adapter);
                } else {
                    adapter.todos = todos;
                    adapter.notifyDataSetChanged();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        todoPlaceholderCard.setOnClickListener(v -> {
            AddTodoDialog dialog = new AddTodoDialog();
            dialog.setDate(currentDate);
            dialog.setRefreshListener(this);
            dialog.show(getParentFragmentManager(), "add_todo");
        });
    }

    private void loadEvents() {
        android.database.Cursor cursor = null;
        try {
            cursor = database.getEventsForDate(currentDate);
            List<EventItem> events = new ArrayList<>();

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int idIdx = cursor.getColumnIndex("id");
                    int titleIdx = cursor.getColumnIndex("title");
                    int descIdx = cursor.getColumnIndex("description");
                    int timeIdx = cursor.getColumnIndex("time");

                    if (idIdx == -1 || titleIdx == -1) continue;

                    long id = cursor.getLong(idIdx);
                    String title = cursor.getString(titleIdx);
                    String description = descIdx != -1 ? cursor.getString(descIdx) : "";
                    String time = timeIdx != -1 ? cursor.getString(timeIdx) : "";
                    events.add(new EventItem(id, title, description, time));
                }
            }
            
            eventPlaceholderCard.setOnClickListener(v -> {
                AddEventDialog dialog = new AddEventDialog();
                dialog.setDate(currentDate);
                dialog.setRefreshListener(this);
                dialog.show(getParentFragmentManager(), "add_event");
            });

            if (events.isEmpty()) {
                eventsRecyclerView.setVisibility(View.GONE);
            } else {
                eventsRecyclerView.setVisibility(View.VISIBLE);
                EventAdapter adapter = (EventAdapter) eventsRecyclerView.getAdapter();
                if (adapter == null) {
                    adapter = new EventAdapter(events);
                    eventsRecyclerView.setAdapter(adapter);
                } else {
                    adapter.events = events;
                    adapter.notifyDataSetChanged();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void loadJournals() {
        android.database.Cursor cursor = null;
        try {
            cursor = database.getJournalForDate(currentDate);
            List<JournalItem> journals = new ArrayList<>();

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int idIdx = cursor.getColumnIndex("id");
                    int contentIdx = cursor.getColumnIndex("content");
                    int photoIdx = cursor.getColumnIndex("photo_path");

                    if (idIdx == -1 || contentIdx == -1) continue;

                    long id = cursor.getLong(idIdx);
                    String content = cursor.getString(contentIdx);
                    String photoPath = photoIdx != -1 ? cursor.getString(photoIdx) : null;
                    journals.add(new JournalItem(id, content, photoPath));
                }
            }

            if (journals.isEmpty()) {
                journalsRecyclerView.setVisibility(View.GONE);
                journalPlaceholderCard.setVisibility(View.VISIBLE);
            } else {
                journalsRecyclerView.setVisibility(View.VISIBLE);
                journalPlaceholderCard.setVisibility(View.GONE);
                JournalAdapter adapter = (JournalAdapter) journalsRecyclerView.getAdapter();
                if (adapter == null) {
                    adapter = new JournalAdapter(journals);
                    journalsRecyclerView.setAdapter(adapter);
                } else {
                    adapter.journals = journals;
                    adapter.notifyDataSetChanged();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        journalPlaceholderCard.setOnClickListener(v -> {
            AddJournalEntryDialog dialog = new AddJournalEntryDialog();
            dialog.setDate(currentDate);
            dialog.setRefreshListener(this);
            dialog.show(getParentFragmentManager(), "add_journal");
        });
    }

    private void loadAlarms() {
        android.database.Cursor cursor = null;
        try {
            cursor = database.getAlarmsForDate(currentDate);
            List<AlarmItem> alarms = new ArrayList<>();

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int idIdx = cursor.getColumnIndex("id");
                    int timeIdx = cursor.getColumnIndex("time");
                    int titleIdx = cursor.getColumnIndex("title");

                    if (idIdx == -1 || timeIdx == -1) continue;

                    long id = cursor.getLong(idIdx);
                    String time = cursor.getString(timeIdx);
                    String title = titleIdx != -1 ? cursor.getString(titleIdx) : "Alarm";
                    alarms.add(new AlarmItem(id, time, title));
                }
            }

            if (alarms.isEmpty()) {
                alarmsRecyclerView.setVisibility(View.GONE);
            } else {
                alarmsRecyclerView.setVisibility(View.VISIBLE);
                AlarmAdapter adapter = (AlarmAdapter) alarmsRecyclerView.getAdapter();
                if (adapter == null) {
                    adapter = new AlarmAdapter(alarms);
                    alarmsRecyclerView.setAdapter(adapter);
                } else {
                    adapter.alarms = alarms;
                    adapter.notifyDataSetChanged();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void loadPhoto() {
        android.util.Log.d("TodayFragment", "loadPhoto called for " + currentDate);
        android.database.Cursor cursor = null;
        try {
            cursor = database.getPhotosForDate(currentDate);
            if (cursor != null && cursor.moveToFirst()) {
                int pathIdx = cursor.getColumnIndex("photo_path");
                String photoPath = pathIdx != -1 ? cursor.getString(pathIdx) : null;
                if (photoPath != null && !photoPath.isEmpty()) {
                    File imageFile = new File(photoPath);
                    if (imageFile.exists()) {
                        showPhotoPreview(Uri.fromFile(imageFile));
                    } else {
                        showPhotoPlaceholder();
                    }
                } else {
                    showPhotoPlaceholder();
                }
            } else {
                showPhotoPlaceholder();
            }
        } catch (Exception e) {
            android.util.Log.e("TodayFragment", "Error loading photo", e);
            showPhotoPlaceholder();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void showPhotoPreview(Uri imageUri) {
        ViewGroup.LayoutParams previewParams = photoPreview.getLayoutParams();
        previewParams.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.3);
        photoPreview.setLayoutParams(previewParams);
        
        Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(photoPreview);
                
        photoPreview.setVisibility(View.VISIBLE);

        photoPlaceholderContainer.setVisibility(View.GONE);
    }

    private void showPhotoPlaceholder() {
        photoPreview.setVisibility(View.GONE);

        photoPlaceholderContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == android.app.Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                File imagesDir = new File(requireContext().getFilesDir(), "images");
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs();
                }
                File imageFile = new File(imagesDir, "photo_" + currentDate + "_" + System.currentTimeMillis() + ".jpg");

                InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
                FileOutputStream outputStream = new FileOutputStream(imageFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.close();
                inputStream.close();

                String photoPath = imageFile.getAbsolutePath();
                database.insertPhoto(currentDate, photoPath, "");
                showPhotoPreview(Uri.fromFile(imageFile));
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Fotoğraf kaydedilemedi", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == android.app.Activity.RESULT_OK && cameraImagePath != null) {
            try {
                File imageFile = new File(cameraImagePath);
                if (imageFile.exists()) {
                    String photoPath = imageFile.getAbsolutePath();
                    database.insertPhoto(currentDate, photoPath, "");
                    showPhotoPreview(Uri.fromFile(imageFile));
                } else {
                    Toast.makeText(requireContext(), "Fotoğraf bulunamadı", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Fotoğraf kaydedilemedi", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupButtons() {
        btnCalendar.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();
                activity.showMonthlyView();
            }
        });

        menuButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });

        btnBackWeekly.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showWeeklyView();
            }
        });
    }

    private void loadMoodForDate() {
        android.database.Cursor cursor = null;
        try {
            cursor = database.getMoodForDate(currentDate);
            if (cursor != null && cursor.moveToFirst()) {
                int moodIdx = cursor.getColumnIndex("mood_type");
                if (moodIdx != -1) {
                    String moodType = cursor.getString(moodIdx);
                    updateMoodButton(moodType);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("TodayFragment", "Error loading mood", e);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void saveMood(String moodType, String feelingTags) {
        android.database.Cursor existing = null;
        try {
            existing = database.getMoodForDate(currentDate);
            if (existing != null && existing.getCount() > 0) {
                database.updateMoodEntry(currentDate, moodType, feelingTags, "");
            } else {
                database.insertMoodEntry(currentDate, moodType, feelingTags, "");
            }
        } catch (Exception e) {
            android.util.Log.e("TodayFragment", "Error saving mood", e);
        } finally {
            if (existing != null) existing.close();
        }
    }

    public class HabitItem {
        long id;
        String name;
        String category;
        String icon;
        boolean completed;
        long createdDateMs;

        HabitItem(long id, String name, String category, String icon, boolean completed, long createdDateMs) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.icon = icon;
            this.completed = completed;
            this.createdDateMs = createdDateMs;
        }
    }

    public class TodoItem {
        public long id;
        public String title;
        public String description;
        public int priority;
        public boolean completed;

        public TodoItem(long id, String title, String description, int priority, boolean completed) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.priority = priority;
            this.completed = completed;
        }
    }

    public class EventItem {
        public long id;
        public String title;
        public String description;
        public String time;

        public EventItem(long id, String title, String description, String time) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.time = time;
        }
    }

    public class JournalItem {
        public long id;
        public String content;
        public String photoPath;

        public JournalItem(long id, String content, String photoPath) {
            this.id = id;
            this.content = content;
            this.photoPath = photoPath;
        }
    }

    public class AlarmItem {
        public long id;
        public String time;
        public String title;

        public AlarmItem(long id, String time, String title) {
            this.id = id;
            this.time = time;
            this.title = title;
        }
    }

    private class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {
        private List<HabitItem> habits;

        HabitAdapter(List<HabitItem> habits) {
            this.habits = habits;
        }

        @NonNull
        @Override
        public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_habit, parent, false);
            return new HabitViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
            HabitItem habit = habits.get(position);
            holder.habitName.setText(habit.name);
            holder.habitCategory.setText(habit.category);
            holder.habitIcon.setText(habit.icon);
            holder.habitCheckbox.setOnCheckedChangeListener(null);
            holder.habitCheckbox.setChecked(habit.completed);

            CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    boolean previousState = habit.completed;
                    if (isDateBeforeCreation(currentDate, habit.createdDateMs)) {
                        buttonView.setOnCheckedChangeListener(null);
                        buttonView.setChecked(previousState);
                        buttonView.setOnCheckedChangeListener(this);
                        Toast.makeText(requireContext(), "Bu alışkanlık oluşturulmadan önceki günlere eklenemez", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long insertResult = database.insertHabitEntry(habit.id, currentDate, isChecked);
                    if (insertResult == -1) {
                        // DB rejected (likely past date), revert UI
                        buttonView.setOnCheckedChangeListener(null);
                        buttonView.setChecked(previousState);
                        buttonView.setOnCheckedChangeListener(this);
                        habit.completed = previousState;
                        Toast.makeText(requireContext(), "Bu alışkanlık oluşturulmadan önceki günlere eklenemez", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    habit.completed = isChecked;
                }
            };
            holder.habitCheckbox.setOnCheckedChangeListener(listener);
        }

        @Override
        public int getItemCount() {
            return habits.size();
        }

        class HabitViewHolder extends RecyclerView.ViewHolder {
            CheckBox habitCheckbox;
            TextView habitIcon;
            TextView habitName;
            TextView habitCategory;

            HabitViewHolder(@NonNull View itemView) {
                super(itemView);
                habitCheckbox = itemView.findViewById(R.id.habit_checkbox);
                habitIcon = itemView.findViewById(R.id.habit_icon);
                habitName = itemView.findViewById(R.id.habit_name);
                habitCategory = itemView.findViewById(R.id.habit_category);
            }
        }
    }

    private class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
        private List<TodoItem> todos;

        TodoAdapter(List<TodoItem> todos) {
            this.todos = todos;
        }

        @NonNull
        @Override
        public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_todo, parent, false);
            return new TodoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
            TodoItem todo = todos.get(position);
            holder.todoCheckbox.setOnCheckedChangeListener(null);
            holder.todoCheckbox.setChecked(todo.completed);
            holder.todoTitle.setText(todo.title);

            // Set priority indicator color
            int priorityColor = getResources().getColor(R.color.primary); // Default Low (0)
            if (todo.priority == 1) priorityColor = android.graphics.Color.parseColor("#FF9800"); // Medium
            else if (todo.priority == 2) priorityColor = android.graphics.Color.parseColor("#F44336"); // High
            holder.priorityIndicator.setBackgroundColor(priorityColor);

            holder.todoCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    database.updateTodoCompletion(todo.id, true);
                    
                    // Update widget
                    HabitTrackerWidget.updateAllWidgets(requireContext());

                    int pos = holder.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        todos.remove(pos);
                        notifyItemRemoved(pos);
                        notifyItemRangeChanged(pos, todos.size());
                    }
                }
            });

            holder.itemView.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setItems(new CharSequence[]{"Edit", "Delete"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Edit
                            AddTodoDialog editDialog = new AddTodoDialog();
                            editDialog.setDate(currentDate);
                            editDialog.setTodoItem(todo);
                            editDialog.setRefreshListener(TodayFragment.this);
                            editDialog.show(getParentFragmentManager(), "edit_todo");
                            break;
                        case 1: // Delete
                            database.deleteTodoItem(todo.id);
                            
                            // Update widget
                            HabitTrackerWidget.updateAllWidgets(requireContext());
                            
                            loadTodos();
                            break;
                    }
                });
                builder.show();
            });
        }

        @Override
        public int getItemCount() {
            return todos.size();
        }

        class TodoViewHolder extends RecyclerView.ViewHolder {
            CheckBox todoCheckbox;
            TextView todoTitle;
            View priorityIndicator;

            TodoViewHolder(@NonNull View itemView) {
                super(itemView);
                todoCheckbox = itemView.findViewById(R.id.todo_checkbox);
                todoTitle = itemView.findViewById(R.id.todo_title);
                priorityIndicator = itemView.findViewById(R.id.priority_indicator);
            }
        }
    }

    private class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
        private List<EventItem> events;

        EventAdapter(List<EventItem> events) {
            this.events = events;
        }

        @NonNull
        @Override
        public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_event, parent, false);
            return new EventViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
            EventItem event = events.get(position);
            holder.eventTitle.setText(event.title);
            holder.eventTime.setText(event.time != null ? event.time : "");

            holder.itemView.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setItems(new CharSequence[]{"Edit", "Delete"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Edit
                            AddEventDialog editDialog = new AddEventDialog();
                            editDialog.setDate(currentDate);
                            editDialog.setEventItem(event);
                            editDialog.setRefreshListener(TodayFragment.this);
                            editDialog.show(getParentFragmentManager(), "edit_event");
                            break;
                        case 1: // Delete
                            database.deleteEvent(event.id);
                            
                            // Update widget
                            HabitTrackerWidget.updateAllWidgets(requireContext());
                            
                            loadEvents();
                            break;
                    }
                });
                builder.show();
            });
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        class EventViewHolder extends RecyclerView.ViewHolder {
            TextView eventTitle;
            TextView eventTime;

            EventViewHolder(@NonNull View itemView) {
                super(itemView);
                eventTitle = itemView.findViewById(R.id.event_title);
                eventTime = itemView.findViewById(R.id.event_time);
            }
        }
    }

    private class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.JournalViewHolder> {
        private List<JournalItem> journals;

        JournalAdapter(List<JournalItem> journals) {
            this.journals = journals;
        }

        @NonNull
        @Override
        public JournalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_journal, parent, false);
            return new JournalViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull JournalViewHolder holder, int position) {
            JournalItem journal = journals.get(position);
            holder.journalContent.setText(journal.content);

            if (!TextUtils.isEmpty(journal.photoPath)) {
                File file = new File(journal.photoPath);
                if (file.exists()) {
                    holder.imageCard.setVisibility(View.VISIBLE);
                    Glide.with(holder.itemView.getContext()).load(file).into(holder.journalImage);
                } else {
                    holder.imageCard.setVisibility(View.GONE);
                }
            } else {
                holder.imageCard.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setItems(new CharSequence[]{"Edit", "Delete"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Edit
                            AddJournalEntryDialog editDialog = new AddJournalEntryDialog();
                            editDialog.setDate(currentDate);
                            editDialog.setJournalItem(journal);
                            editDialog.setRefreshListener(TodayFragment.this);
                            editDialog.show(getParentFragmentManager(), "edit_journal");
                            break;
                        case 1: // Delete
                            database.deleteJournalEntry(journal.id);
                            loadJournals();
                            break;
                    }
                });
                builder.show();
            });
        }

        @Override
        public int getItemCount() {
            return journals.size();
        }

        class JournalViewHolder extends RecyclerView.ViewHolder {
            TextView journalContent;
            View imageCard;
            ImageView journalImage;

            JournalViewHolder(@NonNull View itemView) {
                super(itemView);
                journalContent = itemView.findViewById(R.id.journal_content);
                imageCard = itemView.findViewById(R.id.journal_image_card);
                journalImage = itemView.findViewById(R.id.journal_image);
            }
        }
    }

    private class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
        private List<AlarmItem> alarms;

        AlarmAdapter(List<AlarmItem> alarms) {
            this.alarms = alarms;
        }

        @NonNull
        @Override
        public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_alarm, parent, false);
            return new AlarmViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
            AlarmItem alarm = alarms.get(position);
            holder.alarmTitle.setText(alarm.title);
            holder.alarmTime.setText(alarm.time != null ? alarm.time : "");

            holder.itemView.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setItems(new CharSequence[]{"Edit", "Delete"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Edit
                            AddAlarmDialog editDialog = new AddAlarmDialog();
                            editDialog.setDate(currentDate);
                            editDialog.setAlarmItem(alarm);
                            editDialog.setRefreshListener(TodayFragment.this);
                            editDialog.show(getParentFragmentManager(), "edit_alarm");
                            break;
                        case 1: // Delete
                            cancelAlarm(alarm.id);
                            database.deleteAlarm(alarm.id);
                            loadAlarms();
                            break;
                    }
                });
                builder.show();
            });
        }

        @Override
        public int getItemCount() {
            return alarms.size();
        }

        class AlarmViewHolder extends RecyclerView.ViewHolder {
            TextView alarmTitle;
            TextView alarmTime;

            AlarmViewHolder(@NonNull View itemView) {
                super(itemView);
                alarmTitle = itemView.findViewById(R.id.alarm_title);
                alarmTime = itemView.findViewById(R.id.alarm_time);
            }
        }
    }

    private boolean isDateBeforeCreation(String dateString, long createdDateMs) {
        if (createdDateMs <= 0) return false;
        long entryDay = parseDateToStartOfDay(dateString);
        if (entryDay < 0) return false;
        return entryDay < startOfDay(createdDateMs);
    }

    private long parseDateToStartOfDay(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(dateString));
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTimeInMillis();
        } catch (Exception e) {
            return -1;
        }
    }

    private long startOfDay(long timeInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long parseCreatedDateValue(String raw) {
        if (raw == null || raw.isEmpty()) {
            return startOfDay(System.currentTimeMillis());
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ignored) {
            long fromDateString = parseDateToStartOfDay(raw);
            if (fromDateString > 0) {
                return fromDateString;
            }
            return startOfDay(System.currentTimeMillis());
        }
    }
}
