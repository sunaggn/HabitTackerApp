package com.example.habittracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WeeklyViewFragment extends Fragment {
    private HabitTrackerDatabase database;
    private RecyclerView weeklyRecyclerView;
    private TextView weekTitle;
    private ImageButton menuButton;
    private ImageButton btnBackMonthly;
    private String currentWeekStartDate;
    
    // Item classes
    static class TodoItem {
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

    static class EventItem {
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weekly_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        applyThemeBackground(view);
        
        database = new HabitTrackerDatabase(requireContext());
        weeklyRecyclerView = view.findViewById(R.id.weekly_recycler_view);
        weekTitle = view.findViewById(R.id.week_title);
        menuButton = view.findViewById(R.id.menu_button);
        btnBackMonthly = view.findViewById(R.id.btn_back_monthly);
        
        setupButtons();
        setupWeeklyView();
    }

    private void setupButtons() {
        menuButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });

        btnBackMonthly.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showMonthlyView(currentWeekStartDate);
            }
        });
    }

    private void setupWeeklyView() {
        // Calculate current week start (Monday)
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
        cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        currentWeekStartDate = sdf.format(cal.getTime());
        
        // Update week title
        SimpleDateFormat titleFormat = new SimpleDateFormat("d MMM", Locale.ENGLISH);
        Calendar weekEnd = (Calendar) cal.clone();
        weekEnd.add(Calendar.DAY_OF_YEAR, 6);
        String weekRange = titleFormat.format(cal.getTime()) + " - " + titleFormat.format(weekEnd.getTime());
        weekTitle.setText(weekRange);
        
        List<DayData> weekData = new ArrayList<>();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM", Locale.ENGLISH);
        
        try {
            cal.setTime(sdf.parse(currentWeekStartDate));
            
            for (int i = 0; i < 7; i++) {
                String date = sdf.format(cal.getTime());
                String dayName = dayFormat.format(cal.getTime());
                String dateDisplay = dateFormat.format(cal.getTime());
                
                // Load todos for this day
                List<TodoItem> todos = new ArrayList<>();
                android.database.Cursor todoCursor = database.getTodosForDate(date);
                while (todoCursor.moveToNext()) {
                    long id = todoCursor.getLong(todoCursor.getColumnIndexOrThrow("id"));
                    String title = todoCursor.getString(todoCursor.getColumnIndexOrThrow("title"));
                    String description = todoCursor.getString(todoCursor.getColumnIndexOrThrow("description"));
                    int completed = todoCursor.getInt(todoCursor.getColumnIndexOrThrow("completed"));
                    todos.add(new TodoItem(id, title, description, completed == 1));
                }
                todoCursor.close();
                
                // Load events for this day
                List<EventItem> events = new ArrayList<>();
                android.database.Cursor eventCursor = database.getEventsForDate(date);
                while (eventCursor.moveToNext()) {
                    long id = eventCursor.getLong(eventCursor.getColumnIndexOrThrow("id"));
                    String title = eventCursor.getString(eventCursor.getColumnIndexOrThrow("title"));
                    String description = eventCursor.getString(eventCursor.getColumnIndexOrThrow("description"));
                    String time = eventCursor.getString(eventCursor.getColumnIndexOrThrow("time"));
                    events.add(new EventItem(id, title, description, time));
                }
                eventCursor.close();
                
                weekData.add(new DayData(date, dayName, dateDisplay, todos, events));
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        WeeklyAdapter adapter = new WeeklyAdapter(weekData);
        weeklyRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        weeklyRecyclerView.setAdapter(adapter);
    }

    static class DayData {
        String date;
        String dayName;
        String dateDisplay;
        List<TodoItem> todos;
        List<EventItem> events;

        DayData(String date, String dayName, String dateDisplay, List<TodoItem> todos, List<EventItem> events) {
            this.date = date;
            this.dayName = dayName;
            this.dateDisplay = dateDisplay;
            this.todos = todos;
            this.events = events;
        }
    }

    private class WeeklyAdapter extends RecyclerView.Adapter<WeeklyAdapter.DayViewHolder> {
        private List<DayData> weekData;

        WeeklyAdapter(List<DayData> weekData) {
            this.weekData = weekData;
        }

        @NonNull
        @Override
        public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weekly_day, parent, false);
            return new DayViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
            DayData dayData = weekData.get(position);
            holder.dayNameText.setText(dayData.dayName);
            holder.dateText.setText(dayData.dateDisplay);
            
            // Setup todos
            if (dayData.todos.isEmpty()) {
                holder.todosRecyclerView.setVisibility(View.GONE);
            } else {
                holder.todosRecyclerView.setVisibility(View.VISIBLE);
                WeeklyTodoAdapter todoAdapter = new WeeklyTodoAdapter(dayData.todos);
                holder.todosRecyclerView.setAdapter(todoAdapter);
            }
            
            // Setup events
            if (dayData.events.isEmpty()) {
                holder.eventsRecyclerView.setVisibility(View.GONE);
            } else {
                holder.eventsRecyclerView.setVisibility(View.VISIBLE);
                WeeklyEventAdapter eventAdapter = new WeeklyEventAdapter(dayData.events);
                holder.eventsRecyclerView.setAdapter(eventAdapter);
            }
            
            // Click listener to navigate to day view
            holder.dayCard.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToDate(dayData.date);
                    ((MainActivity) getActivity()).showViewPager();
                }
            });
        }

        @Override
        public int getItemCount() {
            return weekData.size();
        }

        class DayViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView dayCard;
            TextView dayNameText;
            TextView dateText;
            RecyclerView todosRecyclerView;
            RecyclerView eventsRecyclerView;

            DayViewHolder(@NonNull View itemView) {
                super(itemView);
                dayCard = itemView.findViewById(R.id.day_card);
                dayNameText = itemView.findViewById(R.id.day_name);
                dateText = itemView.findViewById(R.id.date_text);
                todosRecyclerView = itemView.findViewById(R.id.todos_recycler_view);
                eventsRecyclerView = itemView.findViewById(R.id.events_recycler_view);
                
                todosRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                eventsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            }
        }
    }

    private class WeeklyTodoAdapter extends RecyclerView.Adapter<WeeklyTodoAdapter.TodoViewHolder> {
        private List<TodoItem> todos;

        WeeklyTodoAdapter(List<TodoItem> todos) {
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
            TextView todoTitle;
            android.widget.CheckBox todoCheckbox;

            TodoViewHolder(@NonNull View itemView) {
                super(itemView);
                todoTitle = itemView.findViewById(R.id.todo_title);
                todoCheckbox = itemView.findViewById(R.id.todo_checkbox);
            }
        }
    }

    private class WeeklyEventAdapter extends RecyclerView.Adapter<WeeklyEventAdapter.EventViewHolder> {
        private List<EventItem> events;

        WeeklyEventAdapter(List<EventItem> events) {
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

    private void applyThemeBackground(View view) {
        android.content.SharedPreferences preferences = requireContext().getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE);
        String theme = preferences.getString("app_mode", "Purple");
        int backgroundRes;
        if ("Green".equals(theme)) {
            backgroundRes = R.drawable.gradient_background_green;
        } else {
            backgroundRes = R.drawable.gradient_background_vibrant;
        }
        View rootView = view.getRootView();
        if (rootView != null) {
            rootView.setBackgroundResource(backgroundRes);
        }
        view.setBackgroundResource(backgroundRes);
    }
}
