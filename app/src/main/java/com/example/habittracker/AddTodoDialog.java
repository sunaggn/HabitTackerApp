package com.example.habittracker;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AddTodoDialog extends DialogFragment {
    private String date;
    private HabitTrackerDatabase database;
    private RecyclerView todoList;
    private TodoListAdapter adapter;
    private ActionBottomSheet.RefreshListener refreshListener;

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
        
        android.view.LayoutInflater inflater = requireActivity().getLayoutInflater();
        android.view.View view = inflater.inflate(R.layout.dialog_add_todo, null);
        
        todoList = view.findViewById(R.id.todo_list);
        todoList.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        List<TodoItem> todos = new ArrayList<>();
        todos.add(new TodoItem("", false));
        adapter = new TodoListAdapter(todos);
        todoList.setAdapter(adapter);
        
        AlertDialog dialog = builder.setView(view)
                .setTitle("Add To-Do")
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Add Item", null)
                .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                for (TodoItem item : todos) {
                    if (!TextUtils.isEmpty(item.title.trim())) {
                        database.insertTodoItem(date, item.title, "", item.priority);
                    }
                }
                Toast.makeText(requireContext(), "To-Do added", Toast.LENGTH_SHORT).show();
                if (refreshListener != null) {
                    refreshListener.onRefresh();
                }
                dialog.dismiss();
            });
            
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                todos.add(new TodoItem("", false));
                adapter.notifyItemInserted(todos.size() - 1);
            });
        });
        
        return dialog;
        
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

    private class TodoListAdapter extends RecyclerView.Adapter<TodoListAdapter.TodoViewHolder> {
        private List<TodoItem> todos;

        TodoListAdapter(List<TodoItem> todos) {
            this.todos = todos;
        }

        @NonNull
        @Override
        public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_todo_input, parent, false);
            return new TodoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
            TodoItem todo = todos.get(position);
            holder.editTitle.setText(todo.title);
            holder.checkbox.setChecked(todo.completed);
            
            holder.editTitle.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    todo.title = holder.editTitle.getText().toString();
                }
            });
            
            holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                todo.completed = isChecked;
            });
        }

        @Override
        public int getItemCount() {
            return todos.size();
        }

        class TodoViewHolder extends RecyclerView.ViewHolder {
            EditText editTitle;
            CheckBox checkbox;

            TodoViewHolder(@NonNull View itemView) {
                super(itemView);
                editTitle = itemView.findViewById(R.id.todo_edit_title);
                checkbox = itemView.findViewById(R.id.todo_checkbox);
            }
        }
    }
}
