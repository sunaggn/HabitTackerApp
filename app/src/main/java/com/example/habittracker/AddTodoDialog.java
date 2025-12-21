package com.example.habittracker;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

public class AddTodoDialog extends DialogFragment {
    private String date;
    private HabitTrackerDatabase database;
    private ActionBottomSheet.RefreshListener refreshListener;
    private LinearLayout todoContainer;
    private List<TodoItem> todos;

    public void setDate(String date) {
        this.date = date;
    }

    public void setRefreshListener(ActionBottomSheet.RefreshListener listener) {
        this.refreshListener = listener;
    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        database = new HabitTrackerDatabase(requireContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_todo, null);
        
        todoContainer = view.findViewById(R.id.todo_container);
        TextView btnSave = view.findViewById(R.id.btn_save);
        TextView btnCancel = view.findViewById(R.id.btn_cancel);
        LinearLayout btnAddItem = view.findViewById(R.id.btn_add_item);
        
        todos = new ArrayList<>();
        addTodoItem();
        
        btnSave.setOnClickListener(v -> saveTodos());
        btnCancel.setOnClickListener(v -> dismiss());
        btnAddItem.setOnClickListener(v -> addTodoItem());
        
        AlertDialog dialog = builder.setView(view).create();
        
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
        
        dialog.getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        );
        
        dialog.setOnShowListener(dialogInterface -> {
            // Focus first EditText after dialog is shown
            if (todoContainer.getChildCount() > 0) {
                View firstItem = todoContainer.getChildAt(0);
                EditText firstEdit = firstItem.findViewById(R.id.todo_edit_title);
                if (firstEdit != null) {
                    firstEdit.post(() -> {
                        firstEdit.requestFocus();
                        InputMethodManager imm = (InputMethodManager) requireContext()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.showSoftInput(firstEdit, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
            }
        });
        
        return dialog;
    }
    
    private void addTodoItem() {
        View itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_todo_input, todoContainer, false);
        
        EditText editText = itemView.findViewById(R.id.todo_edit_title);
        CheckBox checkBox = itemView.findViewById(R.id.todo_checkbox);
        ImageView btnDelete = itemView.findViewById(R.id.btn_delete);
        
        TodoItem todoItem = new TodoItem("", false);
        todos.add(todoItem);
        
        int position = todos.size() - 1;
        
        // Show delete button if more than one item
        if (todos.size() > 1) {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> removeTodoItem(itemView, position));
        } else {
            btnDelete.setVisibility(View.GONE);
        }
        
        // TextWatcher to sync text
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(android.text.Editable s) {
                todoItem.title = s.toString();
                // Update delete button visibility
                if (todos.size() > 1) {
                    btnDelete.setVisibility(View.VISIBLE);
                } else {
                    btnDelete.setVisibility(View.GONE);
                }
            }
        };
        editText.addTextChangedListener(textWatcher);
        
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            todoItem.completed = isChecked;
        });
        
        todoContainer.addView(itemView);
        
        // Focus new item if it's the first one
        if (todos.size() == 1) {
            editText.post(() -> {
                editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) requireContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                }
            });
        }
    }
    
    private void removeTodoItem(View itemView, int position) {
        if (position >= 0 && position < todos.size()) {
            todos.remove(position);
            todoContainer.removeView(itemView);
            
            // If no items left, add one
            if (todos.isEmpty()) {
                addTodoItem();
            }
        }
    }
    
    private void saveTodos() {
        int savedCount = 0;
        for (TodoItem item : todos) {
            String title = item.title != null ? item.title.trim() : "";
            if (!TextUtils.isEmpty(title)) {
                database.insertTodoItem(date, title, "", item.priority);
                savedCount++;
            }
        }
        
        if (savedCount > 0) {
            Toast.makeText(requireContext(), savedCount + " To-Do(s) added", Toast.LENGTH_SHORT).show();
            if (refreshListener != null) {
                refreshListener.onRefresh();
            }
            dismiss();
        } else {
            Toast.makeText(requireContext(), "Please enter at least one To-Do item", Toast.LENGTH_SHORT).show();
        }
    }

    private class TodoItem {
        String title;
        boolean completed;
        int priority;

        TodoItem(String title, boolean completed) {
            this.title = title;
            this.completed = completed;
            this.priority = 0;
        }
    }
}
