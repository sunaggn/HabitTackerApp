package com.example.habittracker;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MonthlyCalendarContentFragment extends Fragment {
    private static final String ARG_MONTH_START_DATE = "month_start_date";
    
    private RecyclerView monthlyRecyclerView;
    private TextView monthTitle;
    private String monthStartDate;
    private HabitTrackerDatabase database;
    private Calendar currentMonth;

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

    static class DayData {
        String date;
        int dayNumber;
        boolean isCurrentMonth;
        boolean isToday;
        List<EventItem> events;

        DayData(String date, int dayNumber, boolean isCurrentMonth, boolean isToday, List<EventItem> events) {
            this.date = date;
            this.dayNumber = dayNumber;
            this.isCurrentMonth = isCurrentMonth;
            this.isToday = isToday;
            this.events = events;
        }
    }

    public static MonthlyCalendarContentFragment newInstance(String monthStartDate) {
        MonthlyCalendarContentFragment fragment = new MonthlyCalendarContentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MONTH_START_DATE, monthStartDate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            monthStartDate = getArguments().getString(ARG_MONTH_START_DATE);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            monthStartDate = sdf.format(cal.getTime());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_monthly_calendar_content, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        applyThemeBackground(view);
        
        database = new HabitTrackerDatabase(requireContext());
        monthlyRecyclerView = view.findViewById(R.id.monthly_recycler_view);
        monthTitle = view.findViewById(R.id.month_title);

        setupMonthlyView();
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

    private void setupMonthlyView() {
        // Determine the month to display
        currentMonth = Calendar.getInstance();
        if (monthStartDate != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                currentMonth.setTime(sdf.parse(monthStartDate));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);

        // Update month title
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
        monthTitle.setText(monthFormat.format(currentMonth.getTime()));

        // Calculate the first day of the calendar grid (first Monday of the month)
        Calendar firstDay = (Calendar) currentMonth.clone();
        int firstDayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK);
        int daysFromMonday = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - Calendar.MONDAY;
        firstDay.add(Calendar.DAY_OF_YEAR, -daysFromMonday);

        // Get today's date
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        // Create list of days (6 weeks = 42 days)
        List<DayData> daysList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        
        Calendar dayCal = (Calendar) firstDay.clone();
        for (int i = 0; i < 42; i++) {
            String date = sdf.format(dayCal.getTime());
            int dayNumber = dayCal.get(Calendar.DAY_OF_MONTH);
            boolean isCurrentMonth = dayCal.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH);
            boolean isToday = dayCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                             dayCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);

            // Load events for this day
            List<EventItem> events = new ArrayList<>();
            android.database.Cursor cursor = database.getEventsForDate(date);
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                events.add(new EventItem(id, title, description, time));
            }
            cursor.close();

            daysList.add(new DayData(date, dayNumber, isCurrentMonth, isToday, events));
            dayCal.add(Calendar.DAY_OF_YEAR, 1);
        }

        MonthlyAdapter adapter = new MonthlyAdapter(daysList);
        monthlyRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        monthlyRecyclerView.setAdapter(adapter);
    }

    private class MonthlyAdapter extends RecyclerView.Adapter<MonthlyAdapter.DayViewHolder> {
        private List<DayData> daysList;

        MonthlyAdapter(List<DayData> daysList) {
            this.daysList = daysList;
        }

        @NonNull
        @Override
        public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_monthly_day, parent, false);
            return new DayViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
            DayData dayData = daysList.get(position);
            
            holder.dayNumber.setText(String.valueOf(dayData.dayNumber));
            
            // Set opacity based on whether it's current month
            if (dayData.isCurrentMonth) {
                holder.dayNumber.setAlpha(1.0f);
            } else {
                holder.dayNumber.setAlpha(0.4f);
            }

            // Highlight today
            if (dayData.isToday) {
                GradientDrawable circle = new GradientDrawable();
                circle.setShape(GradientDrawable.OVAL);
                circle.setColor(ContextCompat.getColor(requireContext(), R.color.primary));
                holder.dayNumber.setBackground(circle);
                holder.dayNumber.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
                holder.dayNumber.setPadding(12, 8, 12, 8);
                holder.dayNumber.setMinWidth(32);
                holder.dayNumber.setMinHeight(32);
                holder.dayNumber.setGravity(android.view.Gravity.CENTER);
            } else {
                holder.dayNumber.setBackground(null);
                holder.dayNumber.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
                holder.dayNumber.setPadding(0, 0, 0, 0);
                holder.dayNumber.setMinWidth(0);
                holder.dayNumber.setMinHeight(0);
            }

            // Clear previous events
            holder.eventsContainer.removeAllViews();

            // Add events (limit to 3 visible, show "+X more" if more)
            int maxVisible = 3;
            for (int i = 0; i < Math.min(dayData.events.size(), maxVisible); i++) {
                EventItem event = dayData.events.get(i);
                TextView eventText = new TextView(requireContext());
                eventText.setText(event.title);
                eventText.setTextSize(10f);
                eventText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
                eventText.setMaxLines(1);
                eventText.setEllipsize(android.text.TextUtils.TruncateAt.END);
                eventText.setPadding(6, 4, 6, 4);
                eventText.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_light));
                eventText.setAlpha(dayData.isCurrentMonth ? 1.0f : 0.4f);
                // Make it rounded
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.RECTANGLE);
                bg.setColor(ContextCompat.getColor(requireContext(), R.color.primary_light));
                bg.setCornerRadius(4f);
                eventText.setBackground(bg);
                eventText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
                ((LinearLayout.LayoutParams) eventText.getLayoutParams()).setMargins(0, 2, 0, 2);
                holder.eventsContainer.addView(eventText);
            }

            if (dayData.events.size() > maxVisible) {
                TextView moreText = new TextView(requireContext());
                moreText.setText("+" + (dayData.events.size() - maxVisible) + " more");
                moreText.setTextSize(9f);
                moreText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
                moreText.setPadding(6, 2, 6, 2);
                moreText.setAlpha(dayData.isCurrentMonth ? 1.0f : 0.4f);
                holder.eventsContainer.addView(moreText);
            }

            // Click listener to navigate to daily view
            holder.itemView.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToDate(dayData.date);
                    ((MainActivity) getActivity()).showViewPager();
                }
            });
        }

        @Override
        public int getItemCount() {
            return daysList.size();
        }

        class DayViewHolder extends RecyclerView.ViewHolder {
            TextView dayNumber;
            LinearLayout eventsContainer;

            DayViewHolder(@NonNull View itemView) {
                super(itemView);
                dayNumber = itemView.findViewById(R.id.day_number);
                eventsContainer = itemView.findViewById(R.id.events_container);
            }
        }
    }
}

