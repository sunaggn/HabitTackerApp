package com.example.habittracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;
import android.widget.ImageButton;

import java.io.File;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private TextView textMode;

    private SharedPreferences preferences;
    private HabitTrackerDatabase database;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        preferences = requireContext()
                .getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE);
        database = HabitTrackerDatabase.getInstance(requireContext());

        textMode = view.findViewById(R.id.text_mode);

        ImageButton btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showViewPager();
            }
        });

        loadSettings();
        setupClickListeners(view);
    }

    // LOAD SETTINGS
    private void loadSettings() {
        // Theme
        String currentMode = preferences.getString("theme_mode", "Light");
        textMode.setText(currentMode);
    }

    // CLICK LISTENERS
    private void setupClickListeners(View view) {

        view.findViewById(R.id.layout_mode)
                .setOnClickListener(v -> showModePicker());

        view.findViewById(R.id.layout_clear_cache)
                .setOnClickListener(v -> showClearCacheDialog());

        view.findViewById(R.id.layout_restart_habits)
                .setOnClickListener(v -> showRestartHabitsDialog());
    }

    // THEME PICKER
    private void showModePicker() {
        String[] modes = {"Light", "Dark"};
        String currentMode = preferences.getString("theme_mode", "Light");

        int selectedIndex = currentMode.equals("Dark") ? 1 : 0;

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Theme")
                .setSingleChoiceItems(modes, selectedIndex, (dialog, which) -> {

                    String selectedMode = modes[which];
                    preferences.edit()
                            .putString("theme_mode", selectedMode)
                            .apply();

                    textMode.setText(selectedMode);

                    int nightMode = selectedMode.equals("Dark")
                            ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                            : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;

                    androidx.appcompat.app.AppCompatDelegate
                            .setDefaultNightMode(nightMode);

                    if (getActivity() != null) {
                        getActivity().recreate();
                    }

                    dialog.dismiss();
                })
                .show();
    }

    // CLEAR ALL DATA
    private void showClearCacheDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Clear All Data")
                .setMessage(
                        "Are you sure you want to delete all data?\n\n" +
                                "• Habits\n" +
                                "• Todos\n" +
                                "• Events\n" +
                                "• Journals\n" +
                                "• Photos\n" +
                                "• Alarms\n\n" +
                                "This action cannot be undone."
                )
                .setPositiveButton("Delete All", (dialog, which) -> clearAllData())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearAllData() {
        try {
            android.database.sqlite.SQLiteDatabase db = database.getWritableDatabase();

            db.delete("habits", null, null);
            db.delete("habit_entries", null, null);
            db.delete("mood_entries", null, null);
            db.delete("journal_entries", null, null);
            db.delete("todo_items", null, null);
            db.delete("events", null, null);
            db.delete("alarms", null, null);
            db.delete("photos", null, null);

            File imagesDir = new File(requireContext().getFilesDir(), "images");
            deleteDirectory(imagesDir);

            Toast.makeText(
                    requireContext(),
                    "All data deleted successfully",
                    Toast.LENGTH_LONG
            ).show();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).refreshTodayFragment();
                ((MainActivity) getActivity()).showViewPager();
            }

        } catch (Exception e) {
            Toast.makeText(
                    requireContext(),
                    "Error deleting data",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void deleteDirectory(File dir) {
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) deleteDirectory(f);
                    else f.delete();
                }
            }
            dir.delete();
        }
    }


    // RESTART HABITS
    private void showRestartHabitsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Restart All Habits")
                .setMessage("This will reset all habit progress.")
                .setPositiveButton("Restart", (dialog, which) ->
                        Toast.makeText(
                                requireContext(),
                                "All habits restarted",
                                Toast.LENGTH_SHORT
                        ).show()
                )
                .setNegativeButton("Cancel", null)
                .show();
    }
}
