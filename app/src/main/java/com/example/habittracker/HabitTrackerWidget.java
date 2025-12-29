package com.example.habittracker;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HabitTrackerWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    /**
     * Update all widget instances
     */
    public static void updateAllWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName widgetComponent = new ComponentName(context, HabitTrackerWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);
        
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        
        // Get today's date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        String today = sdf.format(Calendar.getInstance().getTime());
        
        HabitTrackerDatabase database = HabitTrackerDatabase.getInstance(context);
        
        // Load habits
        StringBuilder habitsText = new StringBuilder();
        android.database.Cursor habitsCursor = null;
        try {
            habitsCursor = database.getHabitsForDate(today);
            if (habitsCursor != null) {
                int count = 0;
                while (habitsCursor.moveToNext() && count < 5) {
                    int nameIdx = habitsCursor.getColumnIndex("name");
                    int iconIdx = habitsCursor.getColumnIndex("icon");
                    int repeatTypeIdx = habitsCursor.getColumnIndex("repeat_type");
                    int daysOfWeekIdx = habitsCursor.getColumnIndex("days_of_week");
                    
                    if (nameIdx == -1) continue;
                    
                    String repeatType = repeatTypeIdx != -1 ? habitsCursor.getString(repeatTypeIdx) : "Daily";
                    String daysOfWeek = daysOfWeekIdx != -1 ? habitsCursor.getString(daysOfWeekIdx) : "";
                    
                    // Check if habit should be shown today
                    if ("Weekly".equals(repeatType) && daysOfWeek != null && !daysOfWeek.isEmpty()) {
                        Calendar calendar = Calendar.getInstance();
                        try {
                            calendar.setTime(sdf.parse(today));
                        } catch (Exception e) {
                            calendar = Calendar.getInstance();
                        }
                        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                        int dayIndex = (currentDayOfWeek == Calendar.SUNDAY) ? 0 : currentDayOfWeek - 1;
                        java.util.List<String> selectedDays = java.util.Arrays.asList(daysOfWeek.split(","));
                        if (!selectedDays.contains(String.valueOf(dayIndex))) {
                            continue;
                        }
                    }
                    
                    String icon = iconIdx != -1 ? habitsCursor.getString(iconIdx) : "📚";
                    String name = habitsCursor.getString(nameIdx);
                    if (habitsText.length() > 0) habitsText.append("\n");
                    habitsText.append(icon).append(" ").append(name);
                    count++;
                }
            }
        } finally {
            if (habitsCursor != null) habitsCursor.close();
        }
        
        if (habitsText.length() == 0) {
            habitsText.append("No habits today");
        }
        views.setTextViewText(R.id.widget_habits, habitsText.toString());
        
        // Load todos
        StringBuilder todosText = new StringBuilder();
        android.database.Cursor todosCursor = null;
        try {
            todosCursor = database.getTodosForDate(today);
            if (todosCursor != null) {
                int count = 0;
                while (todosCursor.moveToNext() && count < 5) {
                    int titleIdx = todosCursor.getColumnIndex("title");
                    int completedIdx = todosCursor.getColumnIndex("completed");
                    
                    if (titleIdx == -1) continue;
                    
                    int completed = completedIdx != -1 ? todosCursor.getInt(completedIdx) : 0;
                    if (completed == 1) continue; // Skip completed todos
                    
                    String title = todosCursor.getString(titleIdx);
                    if (todosText.length() > 0) todosText.append("\n");
                    todosText.append(completed == 1 ? "✓ " : "○ ").append(title);
                    count++;
                }
            }
        } finally {
            if (todosCursor != null) todosCursor.close();
        }
        
        if (todosText.length() == 0) {
            todosText.append("No todos today");
        }
        views.setTextViewText(R.id.widget_todos, todosText.toString());
        
        // Load events
        StringBuilder eventsText = new StringBuilder();
        android.database.Cursor eventsCursor = null;
        try {
            eventsCursor = database.getEventsForDate(today);
            if (eventsCursor != null) {
                int count = 0;
                while (eventsCursor.moveToNext() && count < 5) {
                    int titleIdx = eventsCursor.getColumnIndex("title");
                    int timeIdx = eventsCursor.getColumnIndex("time");
                    
                    if (titleIdx == -1) continue;
                    
                    String title = eventsCursor.getString(titleIdx);
                    String time = timeIdx != -1 ? eventsCursor.getString(timeIdx) : "";
                    if (eventsText.length() > 0) eventsText.append("\n");
                    if (!time.isEmpty()) {
                        eventsText.append("⏰ ").append(time).append(" - ").append(title);
                    } else {
                        eventsText.append("📅 ").append(title);
                    }
                    count++;
                }
            }
        } finally {
            if (eventsCursor != null) eventsCursor.close();
        }
        
        if (eventsText.length() == 0) {
            eventsText.append("No events today");
        }
        views.setTextViewText(R.id.widget_events, eventsText.toString());
        
        // Update date
        SimpleDateFormat displayFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        views.setTextViewText(R.id.widget_date, displayFormat.format(Calendar.getInstance().getTime()));
        
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

