package com.example.habittracker;

import android.app.DatePickerDialog;
import android.database.Cursor;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AddHabitFragment extends Fragment {
    private static final String ARG_HABIT_ID = "habit_id";

    private EditText editHabitName;
    private TextView textSelectedIcon;
    private View viewSelectedColor;
    private Button btnRepeatDaily;
    private Button btnRepeatWeekly;
    private Button btnRepeatMonthly;
    private CheckBox checkAllDays;
    private Button btnDaySun, btnDayMon, btnDayTue, btnDayWed, btnDayThu, btnDayFri, btnDaySat;
    private Switch switchEndDate;
    private Switch switchReminder;
    private TextView textEndDate;
    private Button btnSave;
    private HabitTrackerDatabase database;

    private long habitId = -1;
    private String selectedIcon = "📚";
    private String selectedColor = "#B19CD9";
    private String repeatType = "Daily";
    private List<Boolean> selectedDays = new ArrayList<>();
    private String endDate = "";
    private boolean reminderEnabled = false;

    public static AddHabitFragment newInstance(long habitId) {
        AddHabitFragment fragment = new AddHabitFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_HABIT_ID, habitId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            habitId = getArguments().getLong(ARG_HABIT_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_habit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = new HabitTrackerDatabase(requireContext());
        initializeViews(view);

        for (int i = 0; i < 7; i++) {
            selectedDays.add(true);
        }

        if (habitId != -1) {
            loadHabitData();
        }

        setupClickListeners(view);
        updateColorView();
        updateRepeatButtons();
        updateDayButtons();
    }

    private void initializeViews(View view) {
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
        switchEndDate = view.findViewById(R.id.switch_end_date);
        switchReminder = view.findViewById(R.id.switch_reminder);
        textEndDate = view.findViewById(R.id.text_end_date);
        btnSave = view.findViewById(R.id.btn_save);
        view.findViewById(R.id.btn_back).setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void loadHabitData() {
        Cursor cursor = database.getHabitById(habitId);
        if (cursor != null && cursor.moveToFirst()) {
            editHabitName.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            selectedIcon = cursor.getString(cursor.getColumnIndexOrThrow("icon"));
            textSelectedIcon.setText(selectedIcon);
            selectedColor = cursor.getString(cursor.getColumnIndexOrThrow("color"));
            repeatType = cursor.getString(cursor.getColumnIndexOrThrow("repeat_type"));
            endDate = cursor.getString(cursor.getColumnIndexOrThrow("end_date"));
            reminderEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("reminder_enabled")) == 1;

            String daysOfWeek = cursor.getString(cursor.getColumnIndexOrThrow("days_of_week"));
            if (daysOfWeek != null && !daysOfWeek.isEmpty()) {
                List<String> days = Arrays.asList(daysOfWeek.split(","));
                for(int i=0; i<selectedDays.size(); i++) {
                    selectedDays.set(i, days.contains(String.valueOf(i)));
                }
            }
            
            switchEndDate.setChecked(!TextUtils.isEmpty(endDate));
            textEndDate.setText(endDate);
            switchReminder.setChecked(reminderEnabled);

            cursor.close();
        }
    }

    private void setupClickListeners(View view) {
        // ... (same as before)
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
            if (habitId == -1) {
                database.insertHabit(habitName, "Custom", selectedColor, selectedIcon,
                        repeatType, daysOfWeek, "", endDate, reminderEnabled);
                Toast.makeText(requireContext(), "Habit added", Toast.LENGTH_SHORT).show();
            } else {
                database.updateHabit(habitId, habitName, "Custom", selectedColor, selectedIcon,
                        repeatType, daysOfWeek, "", endDate, reminderEnabled);
                Toast.makeText(requireContext(), "Habit updated", Toast.LENGTH_SHORT).show();
            }

            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();
                activity.showViewPager();
                activity.refreshTodayFragment();
            }

            getParentFragmentManager().popBackStack();
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
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    endDate = sdf.format(calendar.getTime());
                    SimpleDateFormat displayFormat = new SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH);
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
