package com.example.habittracker;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

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

    private TodayFragment.AlarmItem alarmItem; // For editing

    public void setAlarmItem(TodayFragment.AlarmItem alarmItem) {
        this.alarmItem = alarmItem;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        database = HabitTrackerDatabase.getInstance(requireContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        android.view.LayoutInflater inflater = requireActivity().getLayoutInflater();
        android.view.View view = inflater.inflate(R.layout.dialog_add_alarm, null);
        
        editTitle = view.findViewById(R.id.edit_title);
        btnTime = view.findViewById(R.id.btn_time);
        
        // Handle Edit Mode
        if (alarmItem != null) {
            editTitle.setText(alarmItem.title);
            selectedTime = alarmItem.time;
            btnTime.setText(selectedTime);
            // Optionally change title of dialog if layout supported it, but it's simple
        }

        btnTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            // Parse existing time if available
            if (!selectedTime.isEmpty()) {
                try {
                    String[] parts = selectedTime.split(":");
                    calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                    calendar.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
                } catch (Exception ignored) {}
            }

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
            if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(selectedTime)) {
                if (checkPermissions()) {
                    long alarmId;
                    if (alarmItem != null) {
                        database.updateAlarm(alarmItem.id, selectedTime, title);
                        alarmId = alarmItem.id;
                        // Cancel old alarm before scheduling new one (optional but good practice)
                        // We rely on scheduleAlarm to overwrite if ID is same, or we just update DB
                    } else {
                        alarmId = database.insertAlarm(date, selectedTime, title);
                    }

                    if (scheduleAlarm(alarmId, date, selectedTime, title)) {
                        String msg = alarmItem != null ? "Alarm updated" : "Alarm added";
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        if (refreshListener != null) {
                            refreshListener.onRefresh();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Alarm saved but could not schedule", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Please grant notification permission in settings", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
            dismiss();
        });
        
        btnCancel.setOnClickListener(v -> dismiss());
        
        android.app.Dialog dialog = builder.setView(view).create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    private boolean checkPermissions() {
        Context context = requireContext();
        
        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                if (getActivity() != null) {
                    ActivityCompat.requestPermissions(
                            getActivity(),
                            new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                            100
                    );
                }
                return false;
            }
        }
        
        // Check exact alarm permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                // Open settings to grant permission
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return false;
            }
        }
        
        return true;
    }

    private boolean scheduleAlarm(long alarmId, String date, String time, String title) {
        try {
            Context context = requireContext();
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                return false;
            }

            // Parse date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Calendar alarmCalendar = Calendar.getInstance();
            alarmCalendar.setTime(Objects.requireNonNull(dateFormat.parse(date)));

            String[] timeParts = time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            alarmCalendar.set(Calendar.HOUR_OF_DAY, hour);
            alarmCalendar.set(Calendar.MINUTE, minute);
            alarmCalendar.set(Calendar.SECOND, 0);
            alarmCalendar.set(Calendar.MILLISECOND, 0);

            // If the alarm time has passed today, set it for tomorrow
            if (alarmCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
                alarmCalendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            // Create intent for AlarmReceiver
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("alarmId", alarmId);
            intent.putExtra("title", title);
            intent.putExtra("date", date);
            intent.putExtra("time", time);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) alarmId,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            // Schedule alarm
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmCalendar.getTimeInMillis(),
                    pendingIntent
            );

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
