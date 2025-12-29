package com.example.habittracker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import android.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
        database = HabitTrackerDatabase.getInstance(requireContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        android.view.LayoutInflater inflater = requireActivity().getLayoutInflater();
        android.view.View view = inflater.inflate(R.layout.dialog_add_event, null);

        editTitle = view.findViewById(R.id.edit_title);
        editDescription = view.findViewById(R.id.edit_description);
        btnDate = view.findViewById(R.id.btn_date);
        btnTime = view.findViewById(R.id.btn_time);
        checkAlarm = view.findViewById(R.id.check_alarm);

        TextView dialogTitle = view.findViewById(R.id.dialog_title);

        if (eventItem != null) {
            dialogTitle.setText(R.string.edit_event);
            editTitle.setText(eventItem.title);
            editDescription.setText(eventItem.description);
            selectedTime = eventItem.time;
            btnTime.setText(selectedTime);
        } else {
            dialogTitle.setText(R.string.add_event);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat displayFormat = new SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH);
        if (selectedDate != null && !selectedDate.isEmpty()) {
            try {
                Date date = sdf.parse(selectedDate);
                if (date != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    btnDate.setText(displayFormat.format(cal.getTime()));
                }
            } catch (ParseException e) {
                btnDate.setText(R.string.select_date);
            }
        } else {
            btnDate.setText(R.string.select_date);
        }

        btnDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (selectedDate != null && !selectedDate.isEmpty()) {
                try {
                    Date date = sdf.parse(selectedDate);
                    if (date != null) {
                        calendar.setTime(date);
                    }
                } catch (ParseException e) {
                    // Use current date
                }
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

        android.widget.TextView btnSave = view.findViewById(R.id.btn_save);
        android.widget.TextView btnCancel = view.findViewById(R.id.btn_cancel);

        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            if (!TextUtils.isEmpty(title)) {
                String description = editDescription.getText().toString().trim();
                boolean alarmSet = checkAlarm.isChecked();

                if (eventItem != null) {
                    database.updateEvent(eventItem.id, title, description, selectedTime);
                    Toast.makeText(requireContext(), "Event updated", Toast.LENGTH_SHORT).show();
                    
                    // Update widget
                    HabitTrackerWidget.updateAllWidgets(requireContext());
                } else {
                    database.insertEvent(selectedDate, title, description, selectedTime, alarmSet);
                    Toast.makeText(requireContext(), "Event added", Toast.LENGTH_SHORT).show();
                    
                    // Update widget
                    HabitTrackerWidget.updateAllWidgets(requireContext());
                }

                if (refreshListener != null) {
                    refreshListener.onRefresh();
                }
                dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());

        android.app.Dialog dialog = builder.setView(view).create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
            dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        return dialog;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() != null) {
            getActivity().getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }
}
