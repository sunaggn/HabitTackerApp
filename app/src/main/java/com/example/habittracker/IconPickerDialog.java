package com.example.habittracker;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

public class IconPickerDialog extends DialogFragment {
    private IconSelectedListener listener;
    private boolean showEmojis = true;

    public interface IconSelectedListener {
        void onIconSelected(String icon);
    }

    public void setIconSelectedListener(IconSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        android.view.LayoutInflater inflater = requireActivity().getLayoutInflater();
        android.view.View view = inflater.inflate(R.layout.dialog_icon_picker, null);
        
        GridView gridView = view.findViewById(R.id.grid_icons);
        TextView tabIcon = view.findViewById(R.id.tab_icon);
        TextView tabEmoji = view.findViewById(R.id.tab_emoji);
        
        List<String> icons = getIcons();
        IconGridAdapter adapter = new IconGridAdapter(requireContext(), icons);
        gridView.setAdapter(adapter);
        
        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            if (listener != null) {
                listener.onIconSelected(icons.get(position));
            }
            dismiss();
        });
        
        tabIcon.setOnClickListener(v -> {
            showEmojis = false;
            tabIcon.setBackgroundResource(R.drawable.rounded_button);
            tabEmoji.setBackgroundResource(R.drawable.rounded_button_outline);
            List<String> iconList = getIcons();
            IconGridAdapter iconAdapter = new IconGridAdapter(requireContext(), iconList);
            gridView.setAdapter(iconAdapter);
        });
        
        tabEmoji.setOnClickListener(v -> {
            showEmojis = true;
            tabEmoji.setBackgroundResource(R.drawable.rounded_button);
            tabIcon.setBackgroundResource(R.drawable.rounded_button_outline);
            List<String> emojiList = getEmojis();
            IconGridAdapter emojiAdapter = new IconGridAdapter(requireContext(), emojiList);
            gridView.setAdapter(emojiAdapter);
        });
        
        builder.setView(view)
                .setTitle("Choose Icon")
                .setNegativeButton("Cancel", null);
        
        return builder.create();
    }

    private List<String> getIcons() {
        List<String> icons = new ArrayList<>();
        icons.add("⚽"); icons.add("🏆"); icons.add("🥇"); icons.add("🏀"); icons.add("🏂");
        icons.add("🎾"); icons.add("👕"); icons.add("💳"); icons.add("🎳"); icons.add("🎲");
        icons.add("🎯"); icons.add("🎮"); icons.add("🎭"); icons.add("🎫"); icons.add("🎨");
        icons.add("🎣"); icons.add("👁️"); icons.add("🐱"); icons.add("🌵"); icons.add("🧵");
        icons.add("🎉"); icons.add("📚"); icons.add("💊"); icons.add("💼");
        return icons;
    }

    private List<String> getEmojis() {
        List<String> emojis = new ArrayList<>();
        emojis.add("😊"); emojis.add("😄"); emojis.add("😃"); emojis.add("😁"); emojis.add("😆");
        emojis.add("😅"); emojis.add("🤣"); emojis.add("😂"); emojis.add("🙂"); emojis.add("🙃");
        emojis.add("😉"); emojis.add("😋"); emojis.add("😎"); emojis.add("🤩"); emojis.add("🥳");
        emojis.add("😏"); emojis.add("😒"); emojis.add("😞"); emojis.add("😔"); emojis.add("😟");
        emojis.add("😕"); emojis.add("🙁"); emojis.add("😣"); emojis.add("😖"); emojis.add("😫");
        emojis.add("😩"); emojis.add("🥺"); emojis.add("😢"); emojis.add("😭"); emojis.add("😤");
        emojis.add("😠"); emojis.add("😡"); emojis.add("🤬"); emojis.add("🤯"); emojis.add("😳");
        emojis.add("🥵"); emojis.add("🥶"); emojis.add("😱"); emojis.add("😨"); emojis.add("😰");
        emojis.add("😥"); emojis.add("😓"); emojis.add("🤗"); emojis.add("🤔"); emojis.add("🤭");
        emojis.add("🤫"); emojis.add("🤥"); emojis.add("😶"); emojis.add("😐"); emojis.add("😑");
        emojis.add("😬"); emojis.add("🙄"); emojis.add("😯"); emojis.add("😦"); emojis.add("😧");
        emojis.add("😮"); emojis.add("😲"); emojis.add("🥱"); emojis.add("😴"); emojis.add("🤤");
        emojis.add("😪"); emojis.add("😵"); emojis.add("🤐"); emojis.add("🥴"); emojis.add("🤢");
        emojis.add("🤮"); emojis.add("🤧"); emojis.add("😷"); emojis.add("🤒"); emojis.add("🤕");
        emojis.add("🤑"); emojis.add("🤠"); emojis.add("😈"); emojis.add("👿"); emojis.add("👹");
        emojis.add("👺"); emojis.add("🤡"); emojis.add("💩"); emojis.add("👻"); emojis.add("💀");
        emojis.add("☠️"); emojis.add("👽"); emojis.add("👾"); emojis.add("🤖"); emojis.add("🎃");
        return emojis;
    }

    private static class IconGridAdapter extends android.widget.BaseAdapter {
        private android.content.Context context;
        private List<String> icons;

        IconGridAdapter(android.content.Context context, List<String> icons) {
            this.context = context;
            this.icons = icons;
        }

        @Override
        public int getCount() {
            return icons.size();
        }

        @Override
        public Object getItem(int position) {
            return icons.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                textView = new TextView(context);
                textView.setLayoutParams(new GridView.LayoutParams(120, 120));
                textView.setGravity(android.view.Gravity.CENTER);
                textView.setTextSize(32);
                textView.setBackgroundResource(R.drawable.rounded_item_background);
            } else {
                textView = (TextView) convertView;
            }
            textView.setText(icons.get(position));
            return textView;
        }
    }
}

