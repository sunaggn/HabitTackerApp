package com.example.habittracker;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ActionBottomSheet extends BottomSheetDialogFragment {
    private String date;
    private HabitTrackerDatabase database;
    private RefreshListener refreshListener;

    public interface RefreshListener {
        void onRefresh();
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setRefreshListener(RefreshListener listener) {
        this.refreshListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_actions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        database = new HabitTrackerDatabase(requireContext());
        
        LinearLayout addAlarm = view.findViewById(R.id.action_add_alarm);
        LinearLayout addPhoto = view.findViewById(R.id.action_add_photo);
        LinearLayout addJournal = view.findViewById(R.id.action_add_journal);
        LinearLayout addTodo = view.findViewById(R.id.action_add_todo);
        LinearLayout addEvent = view.findViewById(R.id.action_add_event);

        addAlarm.setOnClickListener(v -> {
            AddAlarmDialog dialog = new AddAlarmDialog();
            dialog.setDate(date);
            dialog.setRefreshListener(refreshListener);
            dialog.show(getParentFragmentManager(), "add_alarm");
            dismiss();
        });

        addPhoto.setOnClickListener(v -> {
            AddPhotoDialog dialog = new AddPhotoDialog();
            dialog.setDate(date);
            dialog.setRefreshListener(refreshListener);
            dialog.show(getParentFragmentManager(), "add_photo");
            dismiss();
        });

        addJournal.setOnClickListener(v -> {
            AddJournalDialog dialog = new AddJournalDialog();
            dialog.setDate(date);
            dialog.setRefreshListener(refreshListener);
            dialog.show(getParentFragmentManager(), "add_journal");
            dismiss();
        });

        addTodo.setOnClickListener(v -> {
            AddTodoDialog dialog = new AddTodoDialog();
            dialog.setDate(date);
            dialog.setRefreshListener(refreshListener);
            dialog.show(getParentFragmentManager(), "add_todo");
            dismiss();
        });

        addEvent.setOnClickListener(v -> {
            AddEventDialog dialog = new AddEventDialog();
            dialog.setDate(date);
            dialog.setRefreshListener(refreshListener);
            dialog.show(getParentFragmentManager(), "add_event");
            dismiss();
        });
    }
}

