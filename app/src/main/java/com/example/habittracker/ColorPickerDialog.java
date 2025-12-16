package com.example.habittracker;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

public class ColorPickerDialog extends DialogFragment {
    private ColorSelectedListener listener;

    public interface ColorSelectedListener {
        void onColorSelected(String color);
    }

    public void setColorSelectedListener(ColorSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        android.view.LayoutInflater inflater = requireActivity().getLayoutInflater();
        android.view.View view = inflater.inflate(R.layout.dialog_color_picker, null);
        
        GridView gridView = view.findViewById(R.id.grid_colors);
        
        List<String> colors = getColors();
        ColorGridAdapter adapter = new ColorGridAdapter(requireContext(), colors);
        gridView.setAdapter(adapter);
        
        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            if (listener != null) {
                listener.onColorSelected(colors.get(position));
            }
            dismiss();
        });
        
        builder.setView(view)
                .setTitle("Choose Color")
                .setNegativeButton("Cancel", null);
        
        return builder.create();
    }

    private List<String> getColors() {
        List<String> colors = new ArrayList<>();
        // Pastel colors
        colors.add("#FFB3BA"); colors.add("#FFDFBA"); colors.add("#FFFFBA"); colors.add("#BAFFC9");
        colors.add("#BAE1FF"); colors.add("#E0BBE4"); colors.add("#FEC8D8"); colors.add("#FFDFD3");
        colors.add("#F0E6FF"); colors.add("#E6F3FF"); colors.add("#E0F2F1"); colors.add("#FFF9E6");
        colors.add("#FFE5E5"); colors.add("#E5F5E5"); colors.add("#E5E5FF"); colors.add("#FFE5F5");
        colors.add("#F5E5FF"); colors.add("#E5FFF5"); colors.add("#FFF5E5"); colors.add("#E5F5FF");
        colors.add("#FFE5E0"); colors.add("#E0FFE5"); colors.add("#E0E5FF"); colors.add("#FFE0E5");
        return colors;
    }

    private static class ColorGridAdapter extends android.widget.BaseAdapter {
        private android.content.Context context;
        private List<String> colors;

        ColorGridAdapter(android.content.Context context, List<String> colors) {
            this.context = context;
            this.colors = colors;
        }

        @Override
        public int getCount() {
            return colors.size();
        }

        @Override
        public Object getItem(int position) {
            return colors.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = new View(context);
                view.setLayoutParams(new GridView.LayoutParams(60, 60));
                view.setBackgroundResource(R.drawable.rounded_card);
            } else {
                view = convertView;
            }
            android.graphics.drawable.GradientDrawable drawable = (android.graphics.drawable.GradientDrawable) view.getBackground();
            drawable.setColor(android.graphics.Color.parseColor(colors.get(position)));
            return view;
        }
    }
}

