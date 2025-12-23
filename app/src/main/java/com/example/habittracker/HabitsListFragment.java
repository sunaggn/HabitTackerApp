package com.example.habittracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HabitsListFragment extends Fragment {
    private RecyclerView habitsRecyclerView;
    private HabitTrackerDatabase database;
    private FloatingActionButton fabAdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_habits_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        database = new HabitTrackerDatabase(requireContext());
        habitsRecyclerView = view.findViewById(R.id.habits_recycler_view);
        fabAdd = view.findViewById(R.id.fab_add_habit);
        android.widget.ImageButton btnBack = view.findViewById(R.id.btn_back);
        
        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showViewPager();
            }
        });
        
        habitsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        loadHabits();
        
        fabAdd.setOnClickListener(v -> {
            AddHabitFragment fragment = new AddHabitFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void loadHabits() {
        android.database.Cursor cursor = database.getAllHabits();
        List<HabitItem> habits = new ArrayList<>();
        
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
            habits.add(new HabitItem(id, name, category, false));
        }
        cursor.close();
        
        AllHabitsAdapter adapter = new AllHabitsAdapter(habits);
        habitsRecyclerView.setAdapter(adapter);
    }

    private class HabitItem {
        long id;
        String name;
        String category;
        boolean completed;

        HabitItem(long id, String name, String category, boolean completed) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.completed = completed;
        }
    }

    private class AllHabitsAdapter extends RecyclerView.Adapter<AllHabitsAdapter.HabitViewHolder> {
        private List<HabitItem> habits;

        AllHabitsAdapter(List<HabitItem> habits) {
            this.habits = habits;
        }

        @NonNull
        @Override
        public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_habit, parent, false);
            return new HabitViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
            HabitItem habit = habits.get(position);
            holder.habitName.setText(habit.name);
            holder.habitCategory.setText(habit.category);
            holder.habitCheckbox.setVisibility(View.GONE);

            holder.itemView.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setItems(new CharSequence[]{"Edit", "Delete"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Edit
                            AddHabitFragment fragment = AddHabitFragment.newInstance(habit.id);
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit();
                            break;
                        case 1: // Delete
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Delete Habit")
                                    .setMessage("Are you sure you want to delete \"" + habit.name + "\"?")
                                    .setPositiveButton("Delete", (deleteDialog, deleteWhich) -> {
                                        database.deleteHabit(habit.id);
                                        loadHabits();
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();
                            break;
                    }
                });
                builder.show();
            });
        }

        @Override
        public int getItemCount() {
            return habits.size();
        }

        class HabitViewHolder extends RecyclerView.ViewHolder {
            android.widget.CheckBox habitCheckbox;
            android.widget.TextView habitName;
            android.widget.TextView habitCategory;

            HabitViewHolder(@NonNull View itemView) {
                super(itemView);
                habitCheckbox = itemView.findViewById(R.id.habit_checkbox);
                habitName = itemView.findViewById(R.id.habit_name);
                habitCategory = itemView.findViewById(R.id.habit_category);
            }
        }
    }
}
