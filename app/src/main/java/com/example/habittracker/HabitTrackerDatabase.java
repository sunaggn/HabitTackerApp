package com.example.habittracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class HabitTrackerDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "habit_tracker.db";
    private static final int DATABASE_VERSION = 2;

    // Table names
    private static final String TABLE_USER_PROFILE = "user_profile";
    private static final String TABLE_HABITS = "habits";
    private static final String TABLE_HABIT_ENTRIES = "habit_entries";
    private static final String TABLE_MOOD_ENTRIES = "mood_entries";
    private static final String TABLE_JOURNAL_ENTRIES = "journal_entries";
    private static final String TABLE_TODO_ITEMS = "todo_items";
    private static final String TABLE_EVENTS = "events";
    private static final String TABLE_ALARMS = "alarms";
    private static final String TABLE_PHOTOS = "photos";
    private static final String TABLE_CATEGORIES = "categories";

    public HabitTrackerDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // User Profile table
        db.execSQL("CREATE TABLE " + TABLE_USER_PROFILE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "surname TEXT, " +
                "email TEXT, " +
                "phone TEXT, " +
                "profile_image_path TEXT, " +
                "gender TEXT, " +
                "birthdate TEXT, " +
                "created_date TEXT, " +
                "updated_date TEXT" +
                ")");

        // Categories table
        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT UNIQUE, " +
                "color TEXT, " +
                "is_custom INTEGER DEFAULT 0" +
                ")");

        // Habits table
        db.execSQL("CREATE TABLE " + TABLE_HABITS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "category TEXT, " +
                "color TEXT, " +
                "icon TEXT, " +
                "repeat_type TEXT, " +
                "days_of_week TEXT, " +
                "time_of_day TEXT, " +
                "end_date TEXT, " +
                "reminder_enabled INTEGER DEFAULT 0, " +
                "created_date TEXT" +
                ")");

        // Habit Entries table
        db.execSQL("CREATE TABLE " + TABLE_HABIT_ENTRIES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "habit_id INTEGER, " +
                "date TEXT, " +
                "completed INTEGER DEFAULT 0, " +
                "FOREIGN KEY(habit_id) REFERENCES " + TABLE_HABITS + "(id)" +
                ")");

        // Mood Entries table
        db.execSQL("CREATE TABLE " + TABLE_MOOD_ENTRIES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, " +
                "mood_type TEXT, " +
                "feeling_tags TEXT, " +
                "notes TEXT" +
                ")");

        // Journal Entries table
        db.execSQL("CREATE TABLE " + TABLE_JOURNAL_ENTRIES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, " +
                "content TEXT, " +
                "photo_path TEXT" +
                ")");

        // Todo Items table
        db.execSQL("CREATE TABLE " + TABLE_TODO_ITEMS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, " +
                "title TEXT, " +
                "description TEXT, " +
                "completed INTEGER DEFAULT 0, " +
                "priority INTEGER DEFAULT 0" +
                ")");

        // Events table
        db.execSQL("CREATE TABLE " + TABLE_EVENTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, " +
                "title TEXT, " +
                "description TEXT, " +
                "time TEXT, " +
                "alarm_set INTEGER DEFAULT 0" +
                ")");

        // Alarms table
        db.execSQL("CREATE TABLE " + TABLE_ALARMS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, " +
                "time TEXT, " +
                "title TEXT, " +
                "enabled INTEGER DEFAULT 1" +
                ")");

        // Photos table
        db.execSQL("CREATE TABLE " + TABLE_PHOTOS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, " +
                "photo_path TEXT, " +
                "caption TEXT" +
                ")");

        // Insert default categories
        insertDefaultCategories(db);
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        String[] categories = {"Study", "Sport", "Art", "Health", "Work", "Social", "Finance", "Reading", "Cooking", "Travel"};
        String[] colors = {"#4CAF50", "#2196F3", "#9C27B0", "#F44336", "#FF9800", "#00BCD4", "#FFC107", "#795548", "#E91E63", "#3F51B5"};
        String[] icons = {"📚", "⚽", "🎨", "💊", "💼", "👥", "💰", "📖", "🍳", "✈️"};
        
        for (int i = 0; i < categories.length; i++) {
            ContentValues values = new ContentValues();
            values.put("name", categories[i]);
            values.put("color", colors[i]);
            values.put("is_custom", 0);
            db.insert(TABLE_CATEGORIES, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add new columns to existing tables
            try {
                db.execSQL("ALTER TABLE " + TABLE_USER_PROFILE + " ADD COLUMN gender TEXT");
                db.execSQL("ALTER TABLE " + TABLE_USER_PROFILE + " ADD COLUMN birthdate TEXT");
                db.execSQL("ALTER TABLE " + TABLE_HABITS + " ADD COLUMN repeat_type TEXT");
                db.execSQL("ALTER TABLE " + TABLE_HABITS + " ADD COLUMN days_of_week TEXT");
                db.execSQL("ALTER TABLE " + TABLE_HABITS + " ADD COLUMN time_of_day TEXT");
                db.execSQL("ALTER TABLE " + TABLE_HABITS + " ADD COLUMN end_date TEXT");
                db.execSQL("ALTER TABLE " + TABLE_HABITS + " ADD COLUMN reminder_enabled INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_MOOD_ENTRIES + " ADD COLUMN feeling_tags TEXT");
            } catch (Exception e) {
                // If columns already exist, recreate tables
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_PROFILE);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABITS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABIT_ENTRIES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOOD_ENTRIES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_JOURNAL_ENTRIES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO_ITEMS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARMS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTOS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
                onCreate(db);
            }
        }
    }

    // User Profile methods
    public long insertUserProfile(String name, String surname, String email, String phone, String profileImagePath, String gender, String birthdate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("surname", surname);
        values.put("email", email);
        values.put("phone", phone);
        values.put("profile_image_path", profileImagePath);
        values.put("gender", gender);
        values.put("birthdate", birthdate);
        values.put("created_date", String.valueOf(System.currentTimeMillis()));
        values.put("updated_date", String.valueOf(System.currentTimeMillis()));
        
        // Check if profile exists
        Cursor cursor = db.query(TABLE_USER_PROFILE, null, null, null, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.close();
            return db.update(TABLE_USER_PROFILE, values, null, null);
        }
        cursor.close();
        return db.insert(TABLE_USER_PROFILE, null, values);
    }

    public Cursor getUserProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USER_PROFILE, null, null, null, null, null, null);
    }

    // Habit methods
    public long insertHabit(String name, String category, String color, String icon, String repeatType, String daysOfWeek, String timeOfDay, String endDate, boolean reminderEnabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("category", category);
        values.put("color", color);
        values.put("icon", icon);
        values.put("repeat_type", repeatType);
        values.put("days_of_week", daysOfWeek);
        values.put("time_of_day", timeOfDay);
        values.put("end_date", endDate);
        values.put("reminder_enabled", reminderEnabled ? 1 : 0);
        values.put("created_date", String.valueOf(System.currentTimeMillis()));
        return db.insert(TABLE_HABITS, null, values);
    }

    public Cursor getAllHabits() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_HABITS, null, null, null, null, null, "created_date DESC");
    }

    public Cursor getHabitsForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT h.*, he.completed FROM " + TABLE_HABITS + " h " +
                "LEFT JOIN " + TABLE_HABIT_ENTRIES + " he ON h.id = he.habit_id AND he.date = ? " +
                "ORDER BY h.created_date DESC";
        return db.rawQuery(query, new String[]{date});
    }

    public long insertHabitEntry(long habitId, String date, boolean completed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("habit_id", habitId);
        values.put("date", date);
        values.put("completed", completed ? 1 : 0);
        return db.insert(TABLE_HABIT_ENTRIES, null, values);
    }

    public int updateHabitEntry(long habitId, String date, boolean completed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("completed", completed ? 1 : 0);
        return db.update(TABLE_HABIT_ENTRIES, values, "habit_id = ? AND date = ?", 
                new String[]{String.valueOf(habitId), date});
    }

    public boolean isHabitCompletedForDate(long habitId, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HABIT_ENTRIES, new String[]{"completed"}, 
                "habit_id = ? AND date = ?", new String[]{String.valueOf(habitId), date}, 
                null, null, null);
        boolean completed = false;
        if (cursor.moveToFirst()) {
            completed = cursor.getInt(0) == 1;
        }
        cursor.close();
        return completed;
    }

    // Mood methods
    public long insertMoodEntry(String date, String moodType, String feelingTags, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("mood_type", moodType);
        values.put("feeling_tags", feelingTags);
        values.put("notes", notes);
        return db.insert(TABLE_MOOD_ENTRIES, null, values);
    }

    public Cursor getMoodForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_MOOD_ENTRIES, null, "date = ?", new String[]{date}, null, null, null);
    }

    public int updateMoodEntry(String date, String moodType, String feelingTags, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("mood_type", moodType);
        values.put("feeling_tags", feelingTags);
        values.put("notes", notes);
        return db.update(TABLE_MOOD_ENTRIES, values, "date = ?", new String[]{date});
    }

    // Journal methods
    public long insertJournalEntry(String date, String content, String photoPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("content", content);
        values.put("photo_path", photoPath);
        return db.insert(TABLE_JOURNAL_ENTRIES, null, values);
    }

    public Cursor getJournalForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_JOURNAL_ENTRIES, null, "date = ?", new String[]{date}, null, null, null);
    }

    // Todo methods
    public long insertTodoItem(String date, String title, String description, int priority) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("title", title);
        values.put("description", description);
        values.put("priority", priority);
        values.put("completed", 0);
        return db.insert(TABLE_TODO_ITEMS, null, values);
    }

    public Cursor getTodosForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TODO_ITEMS, null, "date = ?", new String[]{date}, null, null, "priority DESC, id DESC");
    }

    public int updateTodoCompletion(long todoId, boolean completed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("completed", completed ? 1 : 0);
        return db.update(TABLE_TODO_ITEMS, values, "id = ?", new String[]{String.valueOf(todoId)});
    }

    public int deleteTodoItem(long todoId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_TODO_ITEMS, "id = ?", new String[]{String.valueOf(todoId)});
    }

    // Event methods
    public long insertEvent(String date, String title, String description, String time, boolean alarmSet) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("title", title);
        values.put("description", description);
        values.put("time", time);
        values.put("alarm_set", alarmSet ? 1 : 0);
        return db.insert(TABLE_EVENTS, null, values);
    }

    public Cursor getEventsForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EVENTS, null, "date = ?", new String[]{date}, null, null, "time ASC");
    }

    public int deleteEvent(long eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_EVENTS, "id = ?", new String[]{String.valueOf(eventId)});
    }

    // Alarm methods
    public long insertAlarm(String date, String time, String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("time", time);
        values.put("title", title);
        values.put("enabled", 1);
        return db.insert(TABLE_ALARMS, null, values);
    }

    public Cursor getAlarmsForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ALARMS, null, "date = ?", new String[]{date}, null, null, "time ASC");
    }

    // Photo methods
    public long insertPhoto(String date, String photoPath, String caption) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("photo_path", photoPath);
        values.put("caption", caption);
        return db.insert(TABLE_PHOTOS, null, values);
    }

    public Cursor getPhotosForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PHOTOS, null, "date = ?", new String[]{date}, null, null, "id DESC");
    }

    // Category methods
    public long insertCategory(String name, String color, boolean isCustom) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("color", color);
        values.put("is_custom", isCustom ? 1 : 0);
        return db.insert(TABLE_CATEGORIES, null, values);
    }

    public Cursor getAllCategories() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CATEGORIES, null, null, null, null, null, "is_custom ASC, name ASC");
    }

    public String getCategoryColor(String categoryName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES, new String[]{"color"}, 
                "name = ?", new String[]{categoryName}, null, null, null);
        String color = "#4CAF50"; // default color
        if (cursor.moveToFirst()) {
            color = cursor.getString(0);
        }
        cursor.close();
        return color;
    }

    public String getCategoryIcon(String categoryName) {
        String[] categories = {"Study", "Sport", "Art", "Health", "Work", "Social", "Finance", "Reading", "Cooking", "Travel"};
        String[] icons = {"📚", "⚽", "🎨", "💊", "💼", "👥", "💰", "📖", "🍳", "✈️"};
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(categoryName)) {
                return icons[i];
            }
        }
        return "📝"; // default icon
    }
}

