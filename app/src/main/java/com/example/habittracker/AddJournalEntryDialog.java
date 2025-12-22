package com.example.habittracker;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class AddJournalEntryDialog extends DialogFragment {
    private String date;
    private HabitTrackerDatabase database;
    private TodayFragment.JournalItem journalItem; // For editing
    private RefreshListener refreshListener;

    public void setDate(String date) {
        this.date = date;
    }

    public void setRefreshListener(RefreshListener listener) {
        this.refreshListener = listener;
    }

    public void setJournalItem(TodayFragment.JournalItem journalItem) {
        this.journalItem = journalItem;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        database = new HabitTrackerDatabase(requireContext());
        // Use the new transparent dialog theme
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Dialog_Transparent);

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_journal, null);

        EditText journalEditText = view.findViewById(R.id.journal_edit_text);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);

        if (journalItem != null) {
            // Edit mode
            dialogTitle.setText("Edit Journal Entry");
            journalEditText.setText(journalItem.content);
        } else {
            // Add mode
            dialogTitle.setText("Add Journal Entry");
        }

        view.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String content = journalEditText.getText().toString().trim();
            if (TextUtils.isEmpty(content)) {
                Toast.makeText(getContext(), "Content cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (journalItem != null) {
                // Update existing item
                database.updateJournalEntry(journalItem.id, content);
                Toast.makeText(getContext(), "Journal entry updated", Toast.LENGTH_SHORT).show();
            } else {
                // Add new item
                database.insertJournalEntry(date, content);
                Toast.makeText(getContext(), "Journal entry added", Toast.LENGTH_SHORT).show();
            }

            if (refreshListener != null) {
                refreshListener.onRefresh();
            }
            dismiss();
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }
}
