package com.example.habittracker;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

public class MoodDialog extends DialogFragment {
    private MoodSelectedListener listener;
    private String selectedMood = "";
    private List<String> selectedFeelings = new ArrayList<>();
    
    private String[] moods = {"Great", "Good", "Okay", "Not Good", "Bad"};
    private String[] moodEmojis = {"😎", "😊", "😐", "😞", "😢"};
    private String[] feelings = {"Happy", "Brave", "Motivated", "Creative", "Confident", "Calm",
            "Grateful", "Peaceful", "Excited", "Loved", "Hopeful", "Inspired",
            "Proud", "Euphoric", "Nostalgic"};

    public interface MoodSelectedListener {
        void onMoodSelected(String mood, List<String> feelings);
    }

    public void setMoodSelectedListener(MoodSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        android.view.LayoutInflater inflater = requireActivity().getLayoutInflater();
        android.view.View view = inflater.inflate(R.layout.dialog_mood, null);
        
        LinearLayout moodLayout = view.findViewById(R.id.layout_moods);
        LinearLayout feelingsLayout = view.findViewById(R.id.layout_feelings);
        TextView textMoodQuestion = view.findViewById(R.id.text_mood_question);
        TextView textFeelingsQuestion = view.findViewById(R.id.text_feelings_question);
        Button btnSave = view.findViewById(R.id.btn_save_mood);
        
        // Create mood buttons
        for (int i = 0; i < moods.length; i++) {
            Button moodBtn = createMoodButton(moods[i], moodEmojis[i], i);
            moodLayout.addView(moodBtn);
        }
        
        // Create feeling tags
        for (String feeling : feelings) {
            Button feelingBtn = createFeelingButton(feeling);
            feelingsLayout.addView(feelingBtn);
        }
        
        feelingsLayout.setVisibility(View.GONE);
        textFeelingsQuestion.setVisibility(View.GONE);
        
        btnSave.setOnClickListener(v -> {
            if (selectedMood.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), "Please select a mood", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (listener != null) {
                listener.onMoodSelected(selectedMood, selectedFeelings);
            }
            dismiss();
        });
        
        builder.setView(view)
                .setTitle("How is your mood today?")
                .setNegativeButton("Cancel", null);
        
        return builder.create();
    }

    private Button createMoodButton(String mood, String emoji, int index) {
        Button button = new Button(requireContext());
        button.setText(emoji + "\n" + mood);
        button.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        button.setBackgroundResource(R.drawable.rounded_button_outline);
        button.setTextColor(requireContext().getColor(R.color.primary));
        button.setPadding(16, 16, 16, 16);
        
        button.setOnClickListener(v -> {
            selectedMood = mood;
            // Update all mood buttons
            LinearLayout parent = (LinearLayout) v.getParent();
            for (int i = 0; i < parent.getChildCount(); i++) {
                Button btn = (Button) parent.getChildAt(i);
                if (i == index) {
                    btn.setBackgroundResource(R.drawable.rounded_button);
                    btn.setTextColor(android.graphics.Color.WHITE);
                } else {
                    btn.setBackgroundResource(R.drawable.rounded_button_outline);
                    btn.setTextColor(requireContext().getColor(R.color.primary));
                }
            }
            
            // Show feelings section
            View feelingsLayout = getDialog().findViewById(R.id.layout_feelings);
            View textFeelingsQuestion = getDialog().findViewById(R.id.text_feelings_question);
            if (feelingsLayout != null) {
                feelingsLayout.setVisibility(View.VISIBLE);
                textFeelingsQuestion.setVisibility(View.VISIBLE);
            }
            
            // Update save button text
            Button btnSave = getDialog().findViewById(R.id.btn_save_mood);
            if (btnSave != null) {
                btnSave.setText("I Feel " + mood + "!");
            }
        });
        
        return button;
    }

    private Button createFeelingButton(String feeling) {
        Button button = new Button(requireContext());
        button.setText(feeling);
        button.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        button.setBackgroundResource(R.drawable.rounded_button_outline);
        button.setTextColor(requireContext().getColor(R.color.primary));
        button.setPadding(16, 8, 16, 8);
        android.view.ViewGroup.MarginLayoutParams params = (android.view.ViewGroup.MarginLayoutParams) button.getLayoutParams();
        params.setMargins(8, 8, 8, 8);
        button.setLayoutParams(params);
        
        button.setOnClickListener(v -> {
            if (selectedFeelings.contains(feeling)) {
                selectedFeelings.remove(feeling);
                button.setBackgroundResource(R.drawable.rounded_button_outline);
                button.setTextColor(requireContext().getColor(R.color.primary));
            } else {
                selectedFeelings.add(feeling);
                button.setBackgroundResource(R.drawable.rounded_button);
                button.setTextColor(android.graphics.Color.WHITE);
            }
        });
        
        return button;
    }
}

