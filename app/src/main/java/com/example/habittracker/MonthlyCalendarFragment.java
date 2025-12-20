package com.example.habittracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MonthlyCalendarFragment extends Fragment {
    private CalendarView calendarView;
    private ImageButton btnBack;
    private String selectedDate;
    private HabitTrackerDatabase database;

    public void setSelectedDate(String date) {
        this.selectedDate = date;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_monthly_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        database = new HabitTrackerDatabase(requireContext());
        calendarView = view.findViewById(R.id.calendar_view);
        btnBack = view.findViewById(R.id.btn_back);

        // Set selected date if provided
        if (selectedDate != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdf.parse(selectedDate));
                calendarView.setDate(cal);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clickedDayCalendar = eventDay.getCalendar();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String date = sdf.format(clickedDayCalendar.getTime());
            
            // Navigate to weekly calendar
            WeeklyCalendarFragment fragment = new WeeklyCalendarFragment();
            fragment.setSelectedDate(date);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        btnBack.setOnClickListener(v -> {
            // Navigate back to weekly calendar
            WeeklyCalendarFragment fragment = new WeeklyCalendarFragment();
            if (selectedDate != null) {
                fragment.setSelectedDate(selectedDate);
            }
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }
}

