package com.example.habittracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private HabitTrackerDatabase database;
    private Button btnMood;
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
        database = new HabitTrackerDatabase(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_today, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        btnMood = view.findViewById(R.id.btn_mood);
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
        setupPhotoCard();
        setupButtons();

        loadMoodForDate();
        loadPhoto();
    }

    @Override
    public void onRefresh() {
        loadHabits();
        loadTodos();
        loadEvents();
        loadJournals();
        loadPhoto();
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

            android.database.Cursor cursor = database.getMoodForDate(currentDate);
            if (cursor.moveToFirst()) {
                String currentMood = cursor.getString(cursor.getColumnIndexOrThrow("mood_type"));
                dialog.setInitialMood(currentMood);
            }
            cursor.close();

            dialog.setMoodSelectedListener((mood, feelings) -> {
                String feelingTags = String.join(",", feelings);
                saveMood(mood, feelingTags);
                updateMoodButton(mood);
            });
            dialog.show(getParentFragmentManager(), "mood_dialog");
        });
    }

    private void updateMoodButton(String mood) {
        int index = -1;
        for (int i = 0; i < moods.length; i++) {
            if (moods[i].equals(mood)) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            btnMood.setText(moodEmojis[index]);
        }
    }

    private void setupHabitsList() {
        habitsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        loadHabits();
    }

    private void setupTodosList() {
        todosRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        loadTodos();
    }

    private void setupEventsList() {
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        loadEvents();
    }

    private void setupJournalsList() {
        journalsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        loadJournals();
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
        android.database.Cursor cursor = database.getHabitsForDate(currentDate);
        List<HabitItem> habits = new ArrayList<>();
        java.util.Set<Long> seenHabitIds = new java.util.HashSet<>();

        while (cursor.moveToNext()) {
            long habitId = cursor.getLong(cursor.getColumnIndexOrThrow("id"));

            if (seenHabitIds.contains(habitId)) {
                continue;
            }
            seenHabitIds.add(habitId);

            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
            String icon = cursor.getString(cursor.getColumnIndexOrThrow("icon"));
            @SuppressLint("Range") int completed = cursor.getColumnIndex("completed") >= 0 ?
                cursor.getInt(cursor.getColumnIndex("completed")) : 0;
            habits.add(new HabitItem(habitId, name, category, icon, completed == 1));
        }
        cursor.close();

        HabitAdapter adapter = new HabitAdapter(habits);
        habitsRecyclerView.setAdapter(adapter);
    }

    private void loadTodos() {
        android.database.Cursor cursor = database.getTodosForDate(currentDate);
        List<TodoItem> todos = new ArrayList<>();

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            int completed = cursor.getInt(cursor.getColumnIndexOrThrow("completed"));
            todos.add(new TodoItem(id, title, description, completed == 1));
        }
        cursor.close();

        todoPlaceholderCard.setOnClickListener(v -> {
            AddTodoDialog dialog = new AddTodoDialog();
            dialog.setDate(currentDate);
            dialog.setRefreshListener(this);
            dialog.show(getParentFragmentManager(), "add_todo");
        });

        if (todos.isEmpty()) {
            todosRecyclerView.setVisibility(View.GONE);
        } else {
            todosRecyclerView.setVisibility(View.VISIBLE);
            TodoAdapter adapter = new TodoAdapter(todos);
            todosRecyclerView.setAdapter(adapter);
        }
    }

    private void loadEvents() {
        android.database.Cursor cursor = database.getEventsForDate(currentDate);
        List<EventItem> events = new ArrayList<>();

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
            events.add(new EventItem(id, title, description, time));
        }
        cursor.close();

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
            EventAdapter adapter = new EventAdapter(events);
            eventsRecyclerView.setAdapter(adapter);
        }
    }

    private void loadJournals() {
        android.database.Cursor cursor = database.getJournalForDate(currentDate);
        List<JournalItem> journals = new ArrayList<>();

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
            journals.add(new JournalItem(id, content));
        }
        cursor.close();

        journalPlaceholderCard.setOnClickListener(v -> {
            AddJournalEntryDialog dialog = new AddJournalEntryDialog();
            dialog.setDate(currentDate);
            dialog.setRefreshListener(this);
            dialog.show(getParentFragmentManager(), "add_journal");
        });

        if (journals.isEmpty()) {
            journalsRecyclerView.setVisibility(View.GONE);
            journalPlaceholderCard.setVisibility(View.VISIBLE);
        } else {
            journalsRecyclerView.setVisibility(View.VISIBLE);
            journalPlaceholderCard.setVisibility(View.GONE);
            JournalAdapter adapter = new JournalAdapter(journals);
            journalsRecyclerView.setAdapter(adapter);
        }
    }

    private void loadPhoto() {
        android.database.Cursor cursor = database.getPhotosForDate(currentDate);
        if (cursor.moveToFirst()) {
            String photoPath = cursor.getString(cursor.getColumnIndexOrThrow("photo_path"));
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
        cursor.close();
    }

    private void showPhotoPreview(Uri imageUri) {
        ViewGroup.LayoutParams previewParams = photoPreview.getLayoutParams();
        previewParams.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.3);
        photoPreview.setLayoutParams(previewParams);
        photoPreview.setImageURI(imageUri);
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
            WeeklyCalendarFragment fragment = new WeeklyCalendarFragment();
            fragment.setSelectedDate(currentDate);
            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();
                activity.showViewPager();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
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
        android.database.Cursor cursor = database.getMoodForDate(currentDate);
        if (cursor.moveToFirst()) {
            String moodType = cursor.getString(cursor.getColumnIndexOrThrow("mood_type"));
            updateMoodButton(moodType);
        } else {
            btnMood.setText("😐");
        }
        cursor.close();
    }

    private void saveMood(String moodType, String feelingTags) {
        android.database.Cursor existing = database.getMoodForDate(currentDate);
        if (existing.getCount() > 0) {
            database.updateMoodEntry(currentDate, moodType, feelingTags, "");
        } else {
            database.insertMoodEntry(currentDate, moodType, feelingTags, "");
        }
        existing.close();
    }

    public class HabitItem {
        long id;
        String name;
        String category;
        String icon;
        boolean completed;

        HabitItem(long id, String name, String category, String icon, boolean completed) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.icon = icon;
            this.completed = completed;
        }
    }

    public class TodoItem {
        public long id;
        public String title;
        public String description;
        public boolean completed;

        public TodoItem(long id, String title, String description, boolean completed) {
            this.id = id;
            this.title = title;
            this.description = description;
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

        public JournalItem(long id, String content) {
            this.id = id;
            this.content = content;
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

            holder.habitCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    database.insertHabitEntry(habit.id, currentDate, true);

                    int pos = holder.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        habits.remove(pos);
                        notifyItemRemoved(pos);
                        notifyItemRangeChanged(pos, habits.size());
                    }
                }
            });
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

            holder.todoCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    database.updateTodoCompletion(todo.id, true);

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

            TodoViewHolder(@NonNull View itemView) {
                super(itemView);
                todoCheckbox = itemView.findViewById(R.id.todo_checkbox);
                todoTitle = itemView.findViewById(R.id.todo_title);
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

            holder.itemView.setOnLongClickListener(v -> {
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
                            loadEvents();
                            break;
                    }
                });
                builder.show();
                return true;
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

            JournalViewHolder(@NonNull View itemView) {
                super(itemView);
                journalContent = itemView.findViewById(R.id.journal_content);
            }
        }
    }
}
