package com.example.habittracker;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class AddJournalDialog extends DialogFragment {
    private String date;
    private HabitTrackerDatabase database;
    private EditText editContent;
    private ActionBottomSheet.RefreshListener refreshListener;

    public void setDate(String date) {
        this.date = date;
    }

    public void setRefreshListener(ActionBottomSheet.RefreshListener listener) {
        this.refreshListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        database = new HabitTrackerDatabase(requireContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        android.view.LayoutInflater inflater = requireActivity().getLayoutInflater();
        android.view.View view = inflater.inflate(R.layout.dialog_add_journal, null);
        
        editContent = view.findViewById(R.id.edit_content);
        
        builder.setView(view)
                .setTitle("Add Journal Entry")
                .setPositiveButton("Save", (dialog, which) -> {
                    String content = editContent.getText().toString().trim();
                    if (!TextUtils.isEmpty(content)) {
                        database.insertJournalEntry(date, content, "");
                        Toast.makeText(requireContext(), "Journal entry saved", Toast.LENGTH_SHORT).show();
                        if (refreshListener != null) {
                            refreshListener.onRefresh();
                        }
                    }
                })
                .setNegativeButton("Cancel", null);
        
        return builder.create();
    }
}

