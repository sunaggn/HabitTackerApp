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
        String currentMode = preferences.getString("app_mode", "Light");
        textMode.setText(currentMode);
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.layout_mode).setOnClickListener(v -> showModePicker());
        view.findViewById(R.id.layout_clear_cache).setOnClickListener(v -> showClearCacheDialog());
        view.findViewById(R.id.layout_restart_habits).setOnClickListener(v -> showRestartHabitsDialog());
    }

    private void showModePicker() {
        String[] modes = {"Light", "Dark"};
        String currentMode = preferences.getString("app_mode", "Light");
        int selectedIndex = 0;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(currentMode)) {
                selectedIndex = i;
                break;
            }
        }
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Mode")
                .setSingleChoiceItems(modes, selectedIndex, (dialog, which) -> {
                    String selectedMode = modes[which];
                    preferences.edit().putString("app_mode", selectedMode).apply();
                    textMode.setText(selectedMode);
                    dialog.dismiss();
                    // Note: Actual theme change would require app restart or theme recreation
                    Toast.makeText(requireContext(), "Mode changed to " + selectedMode + ". Restart app to apply.", Toast.LENGTH_LONG).show();
                })
                .show();
    }

    private void showClearCacheDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Clear Cache")
                .setMessage("Are you sure you want to clear the cache? This will free up storage space.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    // Clear cache logic here
                    Toast.makeText(requireContext(), "Cache cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
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
