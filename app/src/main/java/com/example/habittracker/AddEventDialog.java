package com.example.habittracker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEventDialog extends DialogFragment {
    private String date;
    private HabitTrackerDatabase database;
    private EditText editTitle;
    private EditText editDescription;
    private Button btnDate;
    private Button btnTime;
    private CheckBox checkAlarm;
    private String selectedTime = "";
    private String selectedDate = "";
    private RefreshListener refreshListener;
    private TodayFragment.EventItem eventItem; // For editing

    public void setDate(String date) {
        this.date = date;
        this.selectedDate = date;
    }

    public void setRefreshListener(RefreshListener listener) {
        this.refreshListener = listener;
    }

    public void setEventItem(TodayFragment.EventItem eventItem) {
        this.eventItem = eventItem;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        database = new HabitTrackerDatabase(requireContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        android.view.LayoutInflater inflater = requireActivity().getLayoutInflater();
        android.view.View view = inflater.inflate(R.layout.dialog_add_event, null);

        editTitle = view.findViewById(R.id.edit_title);
        editDescription = view.findViewById(R.id.edit_description);
        btnDate = view.findViewById(R.id.btn_date);
        btnTime = view.findViewById(R.id.btn_time);
        checkAlarm = view.findViewById(R.id.check_alarm);

        if (eventItem != null) {
            builder.setTitle("Edit Event");
            editTitle.setText(eventItem.title);
            editDescription.setText(eventItem.description);
            selectedTime = eventItem.time;
            btnTime.setText(selectedTime);
        } else {
            builder.setTitle("Add Event");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat displayFormat = new SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH);
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
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = editTitle.getText().toString().trim();
                    if (!TextUtils.isEmpty(title)) {
                        String description = editDescription.getText().toString().trim();
                        boolean alarmSet = checkAlarm.isChecked();
                        
                        if (eventItem != null) {
                            database.updateEvent(eventItem.id, title, description, selectedTime);
                            Toast.makeText(requireContext(), "Event updated", Toast.LENGTH_SHORT).show();
                        } else {
                            database.insertEvent(selectedDate, title, description, selectedTime, alarmSet);
                            Toast.makeText(requireContext(), "Event added", Toast.LENGTH_SHORT).show();
                        }

                        if (refreshListener != null) {
                            refreshListener.onRefresh();
                        }
                    }
                })
                .setNegativeButton("Cancel", null);

        return builder.create();
    }
}
