package com.example.habittracker;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.Random;

public class AddCategoryDialog extends DialogFragment {
    private CategoryAddedListener listener;
    private HabitTrackerDatabase database;

    public interface CategoryAddedListener {
        void onCategoryAdded();
    }

    public void setCategoryAddedListener(CategoryAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        database = new HabitTrackerDatabase(requireContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        android.view.LayoutInflater inflater = requireActivity().getLayoutInflater();
        android.view.View view = inflater.inflate(R.layout.dialog_add_category, null);
        
        EditText editCategoryName = view.findViewById(R.id.edit_category_name);
        
        builder.setView(view)
                .setTitle("Add Custom Category")
                .setPositiveButton("Save", (dialog, which) -> {
                    String categoryName = editCategoryName.getText().toString().trim();
                    if (!TextUtils.isEmpty(categoryName)) {
                        // Generate a random color
                        String[] colors = {"#4CAF50", "#2196F3", "#9C27B0", "#F44336", "#FF9800", 
                                         "#00BCD4", "#FFC107", "#795548", "#E91E63", "#3F51B5"};
                        String color = colors[new Random().nextInt(colors.length)];
                        
                        database.insertCategory(categoryName, color, true);
                        Toast.makeText(requireContext(), "Category added", Toast.LENGTH_SHORT).show();
                        
                        if (listener != null) {
                            listener.onCategoryAdded();
                        }
                    }
                })
                .setNegativeButton("Cancel", null);
        
        return builder.create();
    }
}

