package com.example.habittracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class MoodSliderDialog extends DialogFragment {

    private MoodSelectedListener listener;

    public interface MoodSelectedListener {
        void onMoodSelected(String mood);
    }

    public void setMoodSelectedListener(MoodSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_mood_slider, null);

        SeekBar moodSlider = view.findViewById(R.id.mood_slider);
        TextView moodLabel = view.findViewById(R.id.mood_label);

        String[] moods = {"Very Sad", "Sad", "Neutral", "Happy", "Very Happy"};
        moodSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                moodLabel.setText(moods[progress]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        builder.setView(view)
                .setPositiveButton("Save", (dialog, id) -> {
                    if (listener != null) {
                        listener.onMoodSelected(moods[moodSlider.getProgress()]);
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    // User cancelled the dialog
                });

        return builder.create();
    }
}
