package com.example.habittracker;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class TodayFragment extends Fragment {
    private static final String ARG_DATE = "date";
    private static final int REQUEST_IMAGE_PICK = 1;
    private String currentDate;
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
    private FloatingActionButton fabAdd;
    private ImageButton btnCalendar;
    private ImageButton menuButton;
    private MaterialCardView photoCard;
    private ImageView photoPreview;
    private TextView photoPlaceholder;
    private LinearLayout dayNavigationBoxes;

    private String[] moods = {"Very Sad", "Sad", "Neutral", "Happy", "Very Happy"};
    private String[] moodEmojis = {"😢", "😞", "😐", "😊", "😄"};

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
        fabAdd = view.findViewById(R.id.fab_add);
        btnCalendar = view.findViewById(R.id.btn_calendar);
        menuButton = view.findViewById(R.id.menu_button);
        photoCard = view.findViewById(R.id.photo_card);
        photoPreview = view.findViewById(R.id.photo_preview);
        photoPlaceholder = view.findViewById(R.id.photo_placeholder);
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
    public void onResume() {
        super.onResume();
        loadHabits();
        loadTodos();
        loadEvents();
        loadJournals();
    }

    private void setupDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(currentDate));
            
            SimpleDateFormat displayFormat = new SimpleDateFormat("d MMMM", Locale.ENGLISH);
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
            
            // Get the start of the week (Monday) for the current date
            int dayOfWeek = currentCal.get(Calendar.DAY_OF_WEEK);
            int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
            Calendar weekStart = (Calendar) currentCal.clone();
            weekStart.add(Calendar.DAY_OF_YEAR, -daysFromMonday);
            
            // Set up all 7 day buttons
            TextView[] dayButtons = {dayMonday, dayTuesday, dayWednesday, dayThursday, dayFriday, daySaturday, daySunday};
            
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            
            for (int i = 0; i < 7; i++) {
                Calendar dayCal = (Calendar) weekStart.clone();
                dayCal.add(Calendar.DAY_OF_YEAR, i);
                
                // Calculate offset from current date
                long diff = dayCal.getTimeInMillis() - currentCal.getTimeInMillis();
                final int dayOffset = (int) (diff / (1000 * 60 * 60 * 24));
                
                // Highlight current day
                boolean isCurrentDay = dayCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                      dayCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
                
                // Highlight if it's the displayed date
                boolean isDisplayedDate = dayCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) &&
                                         dayCal.get(Calendar.DAY_OF_YEAR) == currentCal.get(Calendar.DAY_OF_YEAR);
                
                if (isCurrentDay || isDisplayedDate) {
                    dayButtons[i].setBackgroundResource(android.R.drawable.btn_default);
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
            dialog.setMoodSelectedListener((mood, feelings) -> {
                String feelingTags = String.join(",", feelings);
                saveMood(mood, feelingTags);
                updateMoodButton(mood);
            });
            dialog.show(getParentFragmentManager(), "mood_dialog");
        });
    }
    
    private void updateMoodButton(String mood) {
        String[] moodEmojis = {"😎", "😊", "😐", "😞", "😢"};
        int index = -1;
        for (int i = 0; i < moods.length; i++) {
            if (moods[i].equals(mood)) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            btnMood.setText(moodEmojis[index] + " " + mood);
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
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });
    }

    private void loadHabits() {
        android.database.Cursor cursor = database.getHabitsForDate(currentDate);
        List<HabitItem> habits = new ArrayList<>();
        
        while (cursor.moveToNext()) {
            long habitId = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
            int completed = cursor.getInt(cursor.getColumnIndexOrThrow("completed"));
            habits.add(new HabitItem(habitId, name, category, completed == 1));
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
        
        TodoAdapter adapter = new TodoAdapter(todos);
        todosRecyclerView.setAdapter(adapter);
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
        
        EventAdapter adapter = new EventAdapter(events);
        eventsRecyclerView.setAdapter(adapter);
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
        
        JournalAdapter adapter = new JournalAdapter(journals);
        journalsRecyclerView.setAdapter(adapter);
    }

    private void loadPhoto() {
        android.database.Cursor cursor = database.getPhotosForDate(currentDate);
        if (cursor.moveToFirst()) {
            String photoPath = cursor.getString(cursor.getColumnIndexOrThrow("photo_path"));
            if (photoPath != null && !photoPath.isEmpty()) {
                File imageFile = new File(photoPath);
                if (imageFile.exists()) {
                    photoPreview.setImageURI(Uri.fromFile(imageFile));
                    photoPreview.setVisibility(View.VISIBLE);
                    photoPlaceholder.setVisibility(View.GONE);
                }
            }
        }
        cursor.close();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && data != null && data.getData() != null) {
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
                photoPreview.setImageURI(Uri.fromFile(imageFile));
                photoPreview.setVisibility(View.VISIBLE);
                photoPlaceholder.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupButtons() {
        fabAdd.setOnClickListener(v -> {
            ActionBottomSheet bottomSheet = new ActionBottomSheet();
            bottomSheet.setDate(currentDate);
            bottomSheet.setRefreshListener(() -> {
                loadHabits();
                loadTodos();
                loadEvents();
                loadJournals();
                loadPhoto();
            });
            bottomSheet.show(getParentFragmentManager(), "action_bottom_sheet");
        });

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
    }

    private void loadMoodForDate() {
        android.database.Cursor cursor = database.getMoodForDate(currentDate);
        if (cursor.moveToFirst()) {
            String moodType = cursor.getString(cursor.getColumnIndexOrThrow("mood_type"));
            updateMoodButton(moodType);
        } else {
            btnMood.setText("😐 Select Mood");
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

    // Item classes
    private class HabitItem {
        long id;
        String name;
        String category;
        boolean completed;

        HabitItem(long id, String name, String category, boolean completed) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.completed = completed;
        }
    }

    private class TodoItem {
        long id;
        String title;
        String description;
        boolean completed;

        TodoItem(long id, String title, String description, boolean completed) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.completed = completed;
        }
    }

    private class EventItem {
        long id;
        String title;
        String description;
        String time;

        EventItem(long id, String title, String description, String time) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.time = time;
        }
    }

    private class JournalItem {
        long id;
        String content;

        JournalItem(long id, String content) {
            this.id = id;
            this.content = content;
        }
    }

    // Adapters
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
            holder.habitCheckbox.setChecked(habit.completed);
            holder.habitIcon.setText(database.getCategoryIcon(habit.category));
            
            holder.habitCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    database.insertHabitEntry(habit.id, currentDate, true);
                } else {
                    database.updateHabitEntry(habit.id, currentDate, false);
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
            holder.todoTitle.setText(todo.title);
            holder.todoCheckbox.setChecked(todo.completed);
            
            holder.todoCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                database.updateTodoCompletion(todo.id, isChecked);
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
