package com.example.habittracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

public class MoodDialog extends DialogFragment {
    private MoodSelectedListener listener;
    private int selectedMoodIndex = 2; // Default to neutral
    private String initialMood = null; // For loading saved mood
    
    // 5 moods from sad to cool (with glasses)
    private String[] moods = {"Very Sad", "Sad", "Neutral", "Happy", "Very Happy"};
    private String[] moodEmojis = {"😢", "😞", "😐", "😊", "😎"};
    private String[] moodLabels = {"Very Sad", "Sad", "Neutral", "Happy", "Very Happy"};

    public interface MoodSelectedListener {
        void onMoodSelected(String mood, List<String> feelings);
    }

    public void setMoodSelectedListener(MoodSelectedListener listener) {
        this.listener = listener;
    }
    
    public void setInitialMood(String mood) {
        this.initialMood = mood;
        // Find index of the mood
        for (int i = 0; i < moods.length; i++) {
            if (moods[i].equals(mood)) {
                selectedMoodIndex = i;
                break;
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_mood_slider, null);
        
        TextView emojiDisplay = view.findViewById(R.id.emoji_display);
        TextView moodLabel = view.findViewById(R.id.mood_label);
        SeekBar moodSlider = view.findViewById(R.id.mood_slider);
        TextView btnCancel = view.findViewById(R.id.btn_cancel);
        TextView btnSave = view.findViewById(R.id.btn_save);
        
        // Set initial values
        emojiDisplay.setText(moodEmojis[selectedMoodIndex]);
        moodLabel.setText(moodLabels[selectedMoodIndex]);
        moodSlider.setProgress(selectedMoodIndex);
        
        // Update emoji and label when slider changes
        moodSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedMoodIndex = progress;
                emojiDisplay.setText(moodEmojis[progress]);
                moodLabel.setText(moodLabels[progress]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
        });
        
        btnCancel.setOnClickListener(v -> dismiss());
        
        btnSave.setOnClickListener(v -> {
            String selectedMood = moods[selectedMoodIndex];
            if (listener != null) {
                listener.onMoodSelected(selectedMood, new ArrayList<>());
            }
            dismiss();
        });
        
        builder.setView(view);
        
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        return dialog;
    }
}

