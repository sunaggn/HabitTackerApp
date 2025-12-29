package com.example.habittracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class AddTodoDialog extends DialogFragment {

    private String date;
    private HabitTrackerDatabase database;
    private TodayFragment.TodoItem todoItem; // For editing
    private RefreshListener refreshListener;

    public void setDate(String date) {
        this.date = date;
    }

    public void setRefreshListener(RefreshListener listener) {
        this.refreshListener = listener;
    }

    public void setTodoItem(TodayFragment.TodoItem todoItem) {
        this.todoItem = todoItem;
    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        database = HabitTrackerDatabase.getInstance(requireContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_todo, null);

        EditText todoEditText = view.findViewById(R.id.todo_edit_title);
        EditText todoEditDescription = view.findViewById(R.id.todo_edit_description);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        TextView btnSave = view.findViewById(R.id.btn_save);
        TextView btnCancel = view.findViewById(R.id.btn_cancel);
        RadioGroup priorityGroup = view.findViewById(R.id.todo_priority_group);

        if (todoItem != null) {
            // Edit mode
            dialogTitle.setText("Edit To-Do");
            todoEditText.setText(todoItem.title);
            todoEditDescription.setText(todoItem.description);
            
            if (todoItem.priority == 0) priorityGroup.check(R.id.priority_low);
            else if (todoItem.priority == 1) priorityGroup.check(R.id.priority_medium);
            else if (todoItem.priority == 2) priorityGroup.check(R.id.priority_high);
        } else {
            // Add mode
            dialogTitle.setText("Add To-Do");
        }

        btnSave.setOnClickListener(v -> {
            String title = todoEditText.getText().toString().trim();
            String description = todoEditDescription.getText().toString().trim();
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(getContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (todoItem != null) {
                // Update existing item
                int priority = 0;
                int checkedId = priorityGroup.getCheckedRadioButtonId();
                if (checkedId == R.id.priority_medium) priority = 1;
                else if (checkedId == R.id.priority_high) priority = 2;

                database.updateTodoItem(todoItem.id, title, description, priority, todoItem.completed);
                Toast.makeText(getContext(), "To-Do updated", Toast.LENGTH_SHORT).show();
                
                // Update widget
                HabitTrackerWidget.updateAllWidgets(getContext());
            } else {
                // Add new item
                int priority = 0;
                int checkedId = priorityGroup.getCheckedRadioButtonId();
                if (checkedId == R.id.priority_medium) priority = 1;
                else if (checkedId == R.id.priority_high) priority = 2;

                database.insertTodoItem(date, title, description, priority);
                Toast.makeText(getContext(), "To-Do added", Toast.LENGTH_SHORT).show();
                
                // Update widget
                HabitTrackerWidget.updateAllWidgets(getContext());
            }

            if (refreshListener != null) {
                refreshListener.onRefresh();
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());

        AlertDialog dialog = builder.setView(view).create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        dialog.setOnShowListener(dialogInterface -> {
            todoEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(todoEditText, InputMethodManager.SHOW_IMPLICIT);
            todoEditText.setSelection(todoEditText.getText().length());
        });

        return dialog;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() != null) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }
}
