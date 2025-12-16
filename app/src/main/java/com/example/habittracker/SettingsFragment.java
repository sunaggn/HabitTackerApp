package com.example.habittracker;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.Locale;

public class SettingsFragment extends Fragment {
    private Switch switchVacationMode;
    private Switch switchDailyReminder;
    private TextView textMorningTime;
    private TextView textAfternoonTime;
    private TextView textEveningTime;
    private TextView textReminderTime;
    private TextView textFirstDay;
    private SharedPreferences preferences;
    private HabitTrackerDatabase database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        preferences = requireContext().getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE);
        database = new HabitTrackerDatabase(requireContext());
        
        switchVacationMode = view.findViewById(R.id.switch_vacation_mode);
        switchDailyReminder = view.findViewById(R.id.switch_daily_reminder);
        textMorningTime = view.findViewById(R.id.text_morning_time);
        textAfternoonTime = view.findViewById(R.id.text_afternoon_time);
        textEveningTime = view.findViewById(R.id.text_evening_time);
        textReminderTime = view.findViewById(R.id.text_reminder_time);
        textFirstDay = view.findViewById(R.id.text_first_day);
        android.widget.ImageButton btnBack = view.findViewById(R.id.btn_back);
        
        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showViewPager();
            }
        });
        
        loadSettings();
        setupClickListeners(view);
    }

    private void loadSettings() {
        switchVacationMode.setChecked(preferences.getBoolean("vacation_mode", false));
        switchDailyReminder.setChecked(preferences.getBoolean("daily_reminder", true));
        
        textMorningTime.setText("Start at " + preferences.getString("morning_start_time", "05:00"));
        textAfternoonTime.setText("Start at " + preferences.getString("afternoon_start_time", "12:00"));
        textEveningTime.setText("Start at " + preferences.getString("evening_start_time", "18:00"));
        textReminderTime.setText(preferences.getString("reminder_time", "07:00"));
        textFirstDay.setText(preferences.getString("first_day_of_week", "Monday"));
        
        switchVacationMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("vacation_mode", isChecked).apply();
        });
        
        switchDailyReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("daily_reminder", isChecked).apply();
        });
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.layout_morning).setOnClickListener(v -> showTimePicker("morning_start_time", textMorningTime, "Start at "));
        view.findViewById(R.id.layout_afternoon).setOnClickListener(v -> showTimePicker("afternoon_start_time", textAfternoonTime, "Start at "));
        view.findViewById(R.id.layout_evening).setOnClickListener(v -> showTimePicker("evening_start_time", textEveningTime, "Start at "));
        view.findViewById(R.id.layout_reminder_time).setOnClickListener(v -> showTimePicker("reminder_time", textReminderTime, ""));
        view.findViewById(R.id.layout_first_day).setOnClickListener(v -> showDayOfWeekPicker());
        view.findViewById(R.id.layout_clear_cache).setOnClickListener(v -> showClearCacheDialog());
        view.findViewById(R.id.layout_restart_habits).setOnClickListener(v -> showRestartHabitsDialog());
    }

    private void showTimePicker(String key, TextView textView, String prefix) {
        String currentTime = preferences.getString(key, "07:00");
        String[] parts = currentTime.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        
        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                (timePicker, hourOfDay, minute1) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                    preferences.edit().putString(key, time).apply();
                    textView.setText(prefix + time);
                },
                hour, minute, true);
        timePickerDialog.show();
    }

    private void showDayOfWeekPicker() {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        String currentDay = preferences.getString("first_day_of_week", "Monday");
        int selectedIndex = 0;
        for (int i = 0; i < days.length; i++) {
            if (days[i].equals(currentDay)) {
                selectedIndex = i;
                break;
            }
        }
        
        new AlertDialog.Builder(requireContext())
                .setTitle("First Day of Week")
                .setSingleChoiceItems(days, selectedIndex, (dialog, which) -> {
                    preferences.edit().putString("first_day_of_week", days[which]).apply();
                    textFirstDay.setText(days[which]);
                    dialog.dismiss();
                })
                .show();
    }

    private void showClearCacheDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Clear Cache")
                .setMessage("Are you sure you want to clear the cache? This will free up storage space.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    // Clear cache logic here
                    Toast.makeText(requireContext(), "Cache cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRestartHabitsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Restart All Habits")
                .setMessage("Are you sure you want to restart all habits? This will reset all habit progress.")
                .setPositiveButton("Restart", (dialog, which) -> {
                    // Restart habits logic here
                    Toast.makeText(requireContext(), "All habits restarted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
