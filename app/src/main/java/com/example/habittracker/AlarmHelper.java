package com.example.habittracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class AlarmHelper {
    private static final String TAG = "AlarmHelper";

    /**
     * Check if all required permissions are granted
     */
    public static boolean checkPermissions(Context context) {
        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted");
                return false;
            }
        }

        // Check exact alarm permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "SCHEDULE_EXACT_ALARM permission not granted");
                return false;
            }
        }

        return true;
    }

    /**
     * Schedule an alarm
     * @param alarmId Unique ID for the alarm
     * @param date Date in format "yyyy-MM-dd"
     * @param time Time in format "HH:mm"
     * @param title Alarm title
     * @return true if alarm was scheduled successfully
     */
    public static boolean scheduleAlarm(Context context, long alarmId, String date, String time, String title) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e(TAG, "AlarmManager is null");
                return false;
            }

            // Check permissions
            if (!checkPermissions(context)) {
                Log.w(TAG, "Permissions not granted, cannot schedule alarm");
                return false;
            }

            // Parse date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Calendar alarmCalendar = Calendar.getInstance();
            
            try {
                alarmCalendar.setTime(Objects.requireNonNull(dateFormat.parse(date)));
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing date: " + date, e);
                return false;
            }

            String[] timeParts = time.split(":");
            if (timeParts.length != 2) {
                Log.e(TAG, "Invalid time format: " + time);
                return false;
            }

            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            alarmCalendar.set(Calendar.HOUR_OF_DAY, hour);
            alarmCalendar.set(Calendar.MINUTE, minute);
            alarmCalendar.set(Calendar.SECOND, 0);
            alarmCalendar.set(Calendar.MILLISECOND, 0);

            // If the alarm time has passed, set it for tomorrow
            Calendar now = Calendar.getInstance();
            if (alarmCalendar.getTimeInMillis() <= now.getTimeInMillis()) {
                // Check if the date is today or in the past
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);
                
                Calendar alarmDate = (Calendar) alarmCalendar.clone();
                alarmDate.set(Calendar.HOUR_OF_DAY, 0);
                alarmDate.set(Calendar.MINUTE, 0);
                alarmDate.set(Calendar.SECOND, 0);
                alarmDate.set(Calendar.MILLISECOND, 0);
                
                if (alarmDate.getTimeInMillis() <= today.getTimeInMillis()) {
                    // Date is in the past, set for tomorrow
                    alarmCalendar.add(Calendar.DAY_OF_YEAR, 1);
                    Log.d(TAG, "Alarm time has passed, scheduling for tomorrow");
                }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmCalendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        alarmCalendar.getTimeInMillis(),
                        pendingIntent
                );
            }

            Log.d(TAG, "Alarm scheduled: ID=" + alarmId + ", Date=" + date + ", Time=" + time + ", Title=" + title);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling alarm", e);
            return false;
        }
    }

    /**
     * Cancel an alarm
     */
    public static void cancelAlarm(Context context, long alarmId) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e(TAG, "AlarmManager is null");
                return;
            }

            Intent intent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) alarmId,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.d(TAG, "Alarm cancelled: ID=" + alarmId);
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling alarm", e);
        }
    }

    /**
     * Reschedule all active alarms from database
     */
    public static void rescheduleAllAlarms(Context context) {
        HabitTrackerDatabase database = HabitTrackerDatabase.getInstance(context);
        android.database.Cursor cursor = null;
        
        try {
            // Get all alarms from database
            cursor = database.getAllAlarms();
            
            if (cursor != null) {
                int scheduledCount = 0;
                int failedCount = 0;
                
                while (cursor.moveToNext()) {
                    int idIdx = cursor.getColumnIndex("id");
                    int dateIdx = cursor.getColumnIndex("date");
                    int timeIdx = cursor.getColumnIndex("time");
                    int titleIdx = cursor.getColumnIndex("title");
                    int enabledIdx = cursor.getColumnIndex("enabled");

                    if (idIdx == -1 || dateIdx == -1 || timeIdx == -1) continue;

                    long id = cursor.getLong(idIdx);
                    String date = cursor.getString(dateIdx);
                    String time = cursor.getString(timeIdx);
                    String title = titleIdx != -1 ? cursor.getString(titleIdx) : "Alarm";
                    boolean enabled = enabledIdx != -1 ? cursor.getInt(enabledIdx) == 1 : true;

                    if (enabled && date != null && time != null) {
                        if (scheduleAlarm(context, id, date, time, title)) {
                            scheduledCount++;
                        } else {
                            failedCount++;
                        }
                    }
                }
                
                Log.d(TAG, "Rescheduled alarms: " + scheduledCount + " successful, " + failedCount + " failed");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error rescheduling alarms", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}

