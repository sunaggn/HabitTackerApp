package com.example.habittracker;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

public class SettingsFragment extends Fragment {
    private TextView textMode;
    private SharedPreferences preferences;
    private HabitTrackerDatabase database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        preferences = requireContext().getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE);
        database = new HabitTrackerDatabase(requireContext());
        
        textMode = view.findViewById(R.id.text_mode);
        android.widget.ImageButton btnBack = view.findViewById(R.id.btn_back);
        
        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showViewPager();
            }
        });
        
        loadSettings();
        setupClickListeners(view);
    }

    private void loadSettings() {
        // Get current theme mode from preferences
        String currentMode = preferences.getString("theme_mode", "Light");
        textMode.setText(currentMode);
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.layout_mode).setOnClickListener(v -> showModePicker());
        view.findViewById(R.id.layout_clear_cache).setOnClickListener(v -> showClearCacheDialog());
        view.findViewById(R.id.layout_restart_habits).setOnClickListener(v -> showRestartHabitsDialog());
    }

    private void showModePicker() {
        String[] modes = {"Light", "Dark"};
        String currentMode = preferences.getString("theme_mode", "Light");
        int selectedIndex = 0;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(currentMode)) {
                selectedIndex = i;
                break;
            }
        }
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Theme")
                .setSingleChoiceItems(modes, selectedIndex, (dialog, which) -> {
                    String selectedMode = modes[which];
                    preferences.edit().putString("theme_mode", selectedMode).apply();
                    textMode.setText(selectedMode);
                    dialog.dismiss();
                    
                    // Apply theme using AppCompatDelegate
                    int nightMode;
                    if ("Dark".equals(selectedMode)) {
                        nightMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
                    } else {
                        nightMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
                    }
                    androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(nightMode);
                    
                    // Recreate activity to apply theme
                    if (getActivity() != null) {
                        getActivity().recreate();
                    }
                    
                    Toast.makeText(requireContext(), "Theme changed to " + selectedMode, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showClearCacheDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Clear All Data")
                .setMessage("Are you sure you want to delete all data? This will permanently delete:\n\n" +
                        "• User profile\n" +
                        "• All habits and progress\n" +
                        "• Mood entries\n" +
                        "• Photos\n" +
                        "• Todos\n" +
                        "• Events\n" +
                        "• Journals\n" +
                        "• Alarms\n\n" +
                        "This action cannot be undone!")
                .setPositiveButton("Delete All", (dialog, which) -> {
                    clearAllData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearAllData() {
        try {
            android.database.sqlite.SQLiteDatabase db = database.getWritableDatabase();
            long deletedSize = 0;
            int deletedFiles = 0;

            // Delete all data from database tables
            db.delete("user_profile", null, null);
            db.delete("habits", null, null);
            db.delete("habit_entries", null, null);
            db.delete("mood_entries", null, null);
            db.delete("journal_entries", null, null);
            db.delete("todo_items", null, null);
            db.delete("events", null, null);
            db.delete("alarms", null, null);
            db.delete("photos", null, null);
            // Delete only custom categories, keep default ones
            db.delete("categories", "is_custom = ?", new String[]{"1"});

            // Delete all images directory
            File imagesDir = new File(requireContext().getFilesDir(), "images");
            if (imagesDir.exists() && imagesDir.isDirectory()) {
                File[] imageFiles = imagesDir.listFiles();
                if (imageFiles != null) {
                    for (File imageFile : imageFiles) {
                        long fileSize = imageFile.length();
                        if (imageFile.delete()) {
                            deletedSize += fileSize;
                            deletedFiles++;
                        }
                    }
                }
                // Try to delete the directory itself
                imagesDir.delete();
            }

            // Clear app cache directory
            File cacheDir = requireContext().getCacheDir();
            if (cacheDir != null && cacheDir.exists()) {
                deletedSize += deleteDirectory(cacheDir);
                deletedFiles += countFiles(cacheDir);
            }

            // Clear external cache directory if available
            File externalCacheDir = requireContext().getExternalCacheDir();
            if (externalCacheDir != null && externalCacheDir.exists()) {
                deletedSize += deleteDirectory(externalCacheDir);
                deletedFiles += countFiles(externalCacheDir);
            }

            // Clear SharedPreferences (except app_mode if you want to keep it)
            // preferences.edit().clear().apply(); // Uncomment if you want to clear all preferences

            // Format deleted size
            String sizeText;
            if (deletedSize < 1024) {
                sizeText = deletedSize + " B";
            } else if (deletedSize < 1024 * 1024) {
                sizeText = String.format(Locale.ENGLISH, "%.2f KB", deletedSize / 1024.0);
            } else {
                sizeText = String.format(Locale.ENGLISH, "%.2f MB", deletedSize / (1024.0 * 1024.0));
            }

            Toast.makeText(requireContext(), 
                    "All data deleted successfully. " + deletedFiles + " files (" + sizeText + ") removed.", 
                    Toast.LENGTH_LONG).show();

            // Refresh MainActivity if available to update UI and navigate to daily view
            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();
                
                // Show ViewPager (daily view) - this will make the daily view visible
                activity.showViewPager();
                
                // Refresh user profile display
                activity.loadUserProfile();
                
                // Use post to ensure UI is updated after showing ViewPager
                androidx.viewpager2.widget.ViewPager2 viewPager = activity.getViewPager();
                if (viewPager != null) {
                    viewPager.post(() -> {
                        // Force refresh all fragments in ViewPager
                        androidx.viewpager2.adapter.FragmentStateAdapter adapter = 
                            (androidx.viewpager2.adapter.FragmentStateAdapter) viewPager.getAdapter();
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                            
                            // Also refresh adjacent fragments by notifying item changes
                            int currentItem = viewPager.getCurrentItem();
                            for (int i = Math.max(0, currentItem - 1); 
                                 i <= Math.min(adapter.getItemCount() - 1, currentItem + 1); 
                                 i++) {
                                adapter.notifyItemChanged(i);
                            }
                        }
                        
                        // Refresh current fragment - this will call onResume which calls onRefresh
                        activity.refreshTodayFragment();
                    });
                } else {
                    // If ViewPager is null, just refresh the fragment
                    activity.refreshTodayFragment();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error deleting data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private long deleteDirectory(File directory) {
        long deletedSize = 0;
        if (directory != null && directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deletedSize += deleteDirectory(file);
                    } else {
                        deletedSize += file.length();
                        file.delete();
                    }
                }
            }
        }
        return deletedSize;
    }

    private int countFiles(File directory) {
        int count = 0;
        if (directory != null && directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        count += countFiles(file);
                    } else {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private void showRestartHabitsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Restart All Habits")
                .setMessage("Are you sure you want to restart all habits? This will reset all habit progress.")
                .setPositiveButton("Restart", (dialog, which) -> {
                    // Restart habits logic here
                    Toast.makeText(requireContext(), "All habits restarted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
