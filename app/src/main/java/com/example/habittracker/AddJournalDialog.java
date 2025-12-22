package com.example.habittracker;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class AddJournalDialog extends DialogFragment {
    private String date;
    private HabitTrackerDatabase database;
    private EditText editContent;
    private RefreshListener refreshListener;

    public void setDate(String date) {
        this.date = date;
    }

    public void setRefreshListener(RefreshListener listener) {
        this.refreshListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        database = new HabitTrackerDatabase(requireContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_journal, null);
        
        editContent = view.findViewById(R.id.journal_edit_text);
        
        builder.setView(view);
        
        view.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String content = editContent.getText().toString().trim();
            if (!TextUtils.isEmpty(content)) {
                database.insertJournalEntry(date, content);
                Toast.makeText(requireContext(), "Journal entry saved", Toast.LENGTH_SHORT).show();
                if (refreshListener != null) {
                    refreshListener.onRefresh();
                }
                dismiss();
            }
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dismiss());
        
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }
}
