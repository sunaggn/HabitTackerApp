package com.example.habittracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddHabitFragment extends Fragment {
    private EditText editHabitName;
    private TextView textSelectedIcon;
    private View viewSelectedColor;
    private Button btnRepeatDaily;
    private Button btnRepeatWeekly;
    private Button btnRepeatMonthly;
    private CheckBox checkAllDays;
    private Button btnDaySun, btnDayMon, btnDayTue, btnDayWed, btnDayThu, btnDayFri, btnDaySat;
    private Button btnTimeMorning, btnTimeAfternoon, btnTimeEvening;
    private Switch switchEndDate;
    private Switch switchReminder;
    private TextView textEndDate;
    private Button btnSave;
    private HabitTrackerDatabase database;
    
    private String selectedIcon = "📚";
    private String selectedColor = "#B19CD9";
    private String repeatType = "Daily";
    private List<Boolean> selectedDays = new ArrayList<>();
    private String timeOfDay = "";
    private String endDate = "";
    private boolean reminderEnabled = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = new HabitTrackerDatabase(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_habit, container, false);
        
        // Initialize views
        editHabitName = view.findViewById(R.id.edit_habit_name);
        textSelectedIcon = view.findViewById(R.id.text_selected_icon);
        viewSelectedColor = view.findViewById(R.id.view_selected_color);
        btnRepeatDaily = view.findViewById(R.id.btn_repeat_daily);
        btnRepeatWeekly = view.findViewById(R.id.btn_repeat_weekly);
        btnRepeatMonthly = view.findViewById(R.id.btn_repeat_monthly);
        checkAllDays = view.findViewById(R.id.check_all_days);
        btnDaySun = view.findViewById(R.id.btn_day_sun);
        btnDayMon = view.findViewById(R.id.btn_day_mon);
        btnDayTue = view.findViewById(R.id.btn_day_tue);
        btnDayWed = view.findViewById(R.id.btn_day_wed);
        btnDayThu = view.findViewById(R.id.btn_day_thu);
        btnDayFri = view.findViewById(R.id.btn_day_fri);
        btnDaySat = view.findViewById(R.id.btn_day_sat);
        btnTimeMorning = view.findViewById(R.id.btn_time_morning);
        btnTimeAfternoon = view.findViewById(R.id.btn_time_afternoon);
        btnTimeEvening = view.findViewById(R.id.btn_time_evening);
        switchEndDate = view.findViewById(R.id.switch_end_date);
        switchReminder = view.findViewById(R.id.switch_reminder);
        textEndDate = view.findViewById(R.id.text_end_date);
        btnSave = view.findViewById(R.id.btn_save);
        android.widget.ImageButton btnBack = view.findViewById(R.id.btn_back);
        
        // Initialize selected days (all true by default)
        for (int i = 0; i < 7; i++) {
            selectedDays.add(true);
        }
        
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
        
        setupClickListeners(view);
        updateColorView();
        
        return view;
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.layout_icon).setOnClickListener(v -> {
            IconPickerDialog dialog = new IconPickerDialog();
            dialog.setIconSelectedListener(icon -> {
                selectedIcon = icon;
                textSelectedIcon.setText(icon);
            });
            dialog.show(getParentFragmentManager(), "icon_picker");
        });
        
        view.findViewById(R.id.layout_color).setOnClickListener(v -> {
            ColorPickerDialog dialog = new ColorPickerDialog();
            dialog.setColorSelectedListener(color -> {
                selectedColor = color;
                updateColorView();
            });
            dialog.show(getParentFragmentManager(), "color_picker");
        });
        
        btnRepeatDaily.setOnClickListener(v -> {
            repeatType = "Daily";
            updateRepeatButtons();
        });
        
        btnRepeatWeekly.setOnClickListener(v -> {
            repeatType = "Weekly";
            updateRepeatButtons();
        });
        
        btnRepeatMonthly.setOnClickListener(v -> {
            repeatType = "Monthly";
            updateRepeatButtons();
        });
        
        checkAllDays.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (int i = 0; i < 7; i++) {
                selectedDays.set(i, isChecked);
            }
            updateDayButtons();
        });
        
        setupDayButton(btnDaySun, 0);
        setupDayButton(btnDayMon, 1);
        setupDayButton(btnDayTue, 2);
        setupDayButton(btnDayWed, 3);
        setupDayButton(btnDayThu, 4);
        setupDayButton(btnDayFri, 5);
        setupDayButton(btnDaySat, 6);
        
        btnTimeMorning.setOnClickListener(v -> {
            timeOfDay = "Morning";
            updateTimeButtons();
        });
        
        btnTimeAfternoon.setOnClickListener(v -> {
            timeOfDay = "Afternoon";
            updateTimeButtons();
        });
        
        btnTimeEvening.setOnClickListener(v -> {
            timeOfDay = "Evening";
            updateTimeButtons();
        });
        
        switchEndDate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            view.findViewById(R.id.layout_end_date).setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        
        view.findViewById(R.id.layout_end_date).setOnClickListener(v -> showDatePicker());
        
        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            reminderEnabled = isChecked;
        });
        
        btnSave.setOnClickListener(v -> {
            String habitName = editHabitName.getText().toString().trim();
            if (TextUtils.isEmpty(habitName)) {
                Toast.makeText(requireContext(), "Please enter a habit name", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String daysOfWeek = getDaysOfWeekString();
            database.insertHabit(habitName, "Custom", selectedColor, selectedIcon, 
                    repeatType, daysOfWeek, timeOfDay, endDate, reminderEnabled);
            Toast.makeText(requireContext(), "Habit added", Toast.LENGTH_SHORT).show();
            
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setupDayButton(Button button, int index) {
        button.setOnClickListener(v -> {
            selectedDays.set(index, !selectedDays.get(index));
            updateDayButtons();
            checkAllDays.setChecked(selectedDays.stream().allMatch(b -> b));
        });
    }

    private void updateDayButtons() {
        Button[] buttons = {btnDaySun, btnDayMon, btnDayTue, btnDayWed, btnDayThu, btnDayFri, btnDaySat};
        for (int i = 0; i < buttons.length; i++) {
            if (selectedDays.get(i)) {
                buttons[i].setBackgroundResource(R.drawable.rounded_button);
                buttons[i].setTextColor(android.graphics.Color.WHITE);
            } else {
                buttons[i].setBackgroundResource(R.drawable.rounded_button_outline);
                buttons[i].setTextColor(requireContext().getColor(R.color.primary));
            }
        }
    }

    private void updateRepeatButtons() {
        btnRepeatDaily.setBackgroundResource(repeatType.equals("Daily") ? 
                R.drawable.rounded_button : R.drawable.rounded_button_outline);
        btnRepeatDaily.setTextColor(requireContext().getColor(repeatType.equals("Daily") ? 
                android.R.color.white : R.color.primary));
        
        btnRepeatWeekly.setBackgroundResource(repeatType.equals("Weekly") ? 
                R.drawable.rounded_button : R.drawable.rounded_button_outline);
        btnRepeatWeekly.setTextColor(requireContext().getColor(repeatType.equals("Weekly") ? 
                android.R.color.white : R.color.primary));
        
        btnRepeatMonthly.setBackgroundResource(repeatType.equals("Monthly") ? 
                R.drawable.rounded_button : R.drawable.rounded_button_outline);
        btnRepeatMonthly.setTextColor(requireContext().getColor(repeatType.equals("Monthly") ? 
                android.R.color.white : R.color.primary));
    }

    private void updateTimeButtons() {
        btnTimeMorning.setBackgroundResource(timeOfDay.equals("Morning") ? 
                R.drawable.rounded_button : R.drawable.rounded_button_outline);
        btnTimeMorning.setTextColor(requireContext().getColor(timeOfDay.equals("Morning") ? 
                android.R.color.white : R.color.primary));
        
        btnTimeAfternoon.setBackgroundResource(timeOfDay.equals("Afternoon") ? 
                R.drawable.rounded_button : R.drawable.rounded_button_outline);
        btnTimeAfternoon.setTextColor(requireContext().getColor(timeOfDay.equals("Afternoon") ? 
                android.R.color.white : R.color.primary));
        
        btnTimeEvening.setBackgroundResource(timeOfDay.equals("Evening") ? 
                R.drawable.rounded_button : R.drawable.rounded_button_outline);
        btnTimeEvening.setTextColor(requireContext().getColor(timeOfDay.equals("Evening") ? 
                android.R.color.white : R.color.primary));
    }

    private void updateColorView() {
        android.graphics.drawable.GradientDrawable drawable = 
                (android.graphics.drawable.GradientDrawable) viewSelectedColor.getBackground();
        if (drawable == null) {
            drawable = new android.graphics.drawable.GradientDrawable();
            drawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            viewSelectedColor.setBackground(drawable);
        }
        drawable.setColor(android.graphics.Color.parseColor(selectedColor));
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    endDate = sdf.format(calendar.getTime());
                    SimpleDateFormat displayFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
                    textEndDate.setText(displayFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private String getDaysOfWeekString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedDays.size(); i++) {
            if (selectedDays.get(i)) {
                if (sb.length() > 0) sb.append(",");
                sb.append(i);
            }
        }
        return sb.toString();
    }
}
