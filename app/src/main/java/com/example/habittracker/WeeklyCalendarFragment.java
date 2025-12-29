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
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class WeeklyCalendarFragment extends Fragment {
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
        return inflater.inflate(R.layout.fragment_weekly_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        database = HabitTrackerDatabase.getInstance(requireContext());
        calendarView = view.findViewById(R.id.calendar_view);
        btnBack = view.findViewById(R.id.btn_back);

        // Set calendar to weekly view
        calendarView.setHeaderVisibility(View.GONE);
        
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
            
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToDate(date);
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });

        btnBack.setOnClickListener(v -> {
            // Navigate to monthly calendar
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showMonthlyView();
            }
        });
    }
}

