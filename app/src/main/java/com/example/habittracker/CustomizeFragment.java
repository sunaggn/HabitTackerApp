package com.example.habittracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;
import android.graphics.Color;

public class CustomizeFragment extends Fragment {
    private SeekBar seekBarRed;
    private SeekBar seekBarGreen;
    private SeekBar seekBarBlue;
    private TextView previewColor;
    private Button btnSave;
    private SharedPreferences preferences;
    private int currentRed = 33;
    private int currentGreen = 150;
    private int currentBlue = 243;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customize, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        preferences = requireContext().getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE);
        seekBarRed = view.findViewById(R.id.seekbar_red);
        seekBarGreen = view.findViewById(R.id.seekbar_green);
        seekBarBlue = view.findViewById(R.id.seekbar_blue);
        previewColor = view.findViewById(R.id.preview_color);
        btnSave = view.findViewById(R.id.btn_save_colors);
        android.widget.ImageButton btnBack = view.findViewById(R.id.btn_back);
        
        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showViewPager();
            }
        });
        
        loadColors();
        setupSeekBars();
        
        btnSave.setOnClickListener(v -> {
            int color = Color.rgb(currentRed, currentGreen, currentBlue);
            preferences.edit()
                    .putInt("primary_color", color)
                    .putInt("color_red", currentRed)
                    .putInt("color_green", currentGreen)
                    .putInt("color_blue", currentBlue)
                    .apply();
            android.widget.Toast.makeText(requireContext(), "Colors saved", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void loadColors() {
        currentRed = preferences.getInt("color_red", 33);
        currentGreen = preferences.getInt("color_green", 150);
        currentBlue = preferences.getInt("color_blue", 243);
        updatePreview();
    }

    private void setupSeekBars() {
        seekBarRed.setMax(255);
        seekBarRed.setProgress(currentRed);
        seekBarGreen.setMax(255);
        seekBarGreen.setProgress(currentGreen);
        seekBarBlue.setMax(255);
        seekBarBlue.setProgress(currentBlue);
        
        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar == seekBarRed) {
                    currentRed = progress;
                } else if (seekBar == seekBarGreen) {
                    currentGreen = progress;
                } else if (seekBar == seekBarBlue) {
                    currentBlue = progress;
                }
                updatePreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };
        
        seekBarRed.setOnSeekBarChangeListener(listener);
        seekBarGreen.setOnSeekBarChangeListener(listener);
        seekBarBlue.setOnSeekBarChangeListener(listener);
    }

    private void updatePreview() {
        int color = Color.rgb(currentRed, currentGreen, currentBlue);
        previewColor.setBackgroundColor(color);
        previewColor.setText(String.format("#%02X%02X%02X", currentRed, currentGreen, currentBlue));
    }
}

