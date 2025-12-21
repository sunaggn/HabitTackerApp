package com.example.habittracker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddAlarmDialog extends DialogFragment {
    private String date;
    private HabitTrackerDatabase database;
    private EditText editTitle;
    private Button btnDate;
    private Button btnTime;
    private String selectedTime = "";
    private String selectedDate = "";
    private ActionBottomSheet.RefreshListener refreshListener;

    public void setDate(String date) {
        this.date = date;
        this.selectedDate = date;
    }

    public void setRefreshListener(ActionBottomSheet.RefreshListener listener) {
        this.refreshListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        database = new HabitTrackerDatabase(requireContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        android.view.LayoutInflater inflater = requireActivity().getLayoutInflater();
        android.view.View view = inflater.inflate(R.layout.dialog_add_alarm, null);
        
        editTitle = view.findViewById(R.id.edit_title);
        btnDate = view.findViewById(R.id.btn_date);
        btnTime = view.findViewById(R.id.btn_time);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat displayFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(selectedDate));
            btnDate.setText(displayFormat.format(cal.getTime()));
        } catch (Exception e) {
            btnDate.setText("Select Date");
        }
        
        btnDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            try {
                calendar.setTime(sdf.parse(selectedDate));
            } catch (Exception e) {
                // Use current date
            }
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        selectedDate = sdf.format(calendar.getTime());
                        btnDate.setText(displayFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
        
        btnTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                    (view1, hourOfDay, minute) -> {
                        selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        btnTime.setText(selectedTime);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true);
            timePickerDialog.show();
        });
        
        builder.setView(view)
                .setTitle("Add Alarm")
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = editTitle.getText().toString().trim();
                    if (TextUtils.isEmpty(title)) {
                        title = "Alarm";
                    }
                    if (!TextUtils.isEmpty(selectedTime) && !TextUtils.isEmpty(selectedDate)) {
                        database.insertAlarm(selectedDate, selectedTime, title);
                        Toast.makeText(requireContext(), "Alarm added", Toast.LENGTH_SHORT).show();
                        if (refreshListener != null) {
                            refreshListener.onRefresh();
                        }
                    }
                })
                .setNegativeButton("Cancel", null);
        
        return builder.create();
    }
}

