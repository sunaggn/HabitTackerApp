package com.example.habittracker;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Locale;

public class AddAlarmDialog extends DialogFragment {
    private String date;
    private HabitTrackerDatabase database;
    private EditText editTitle;
    private Button btnTime;
    private String selectedTime = "";
    private RefreshListener refreshListener;

    public void setDate(String date) {
        this.date = date;
    }

    public void setRefreshListener(RefreshListener listener) {
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
        btnTime = view.findViewById(R.id.btn_time);
        
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
                    if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(selectedTime)) {
                        database.insertAlarm(date, selectedTime, title);
                        Toast.makeText(requireContext(), "Alarm added", Toast.LENGTH_SHORT).show();
                        // if (refreshListener != null) { // No refresh needed for alarms yet
                        //     refreshListener.onRefresh();
                        // }
                    }
                })
                .setNegativeButton("Cancel", null);
        
        return builder.create();
    }
}
