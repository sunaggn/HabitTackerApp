package com.example.habittracker;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.bumptech.glide.Glide;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AddJournalEntryDialog extends DialogFragment {
    private String date;
    private HabitTrackerDatabase database;
    private TodayFragment.JournalItem journalItem; // For editing
    private RefreshListener refreshListener;
    private String journalImagePath = "";
    private static final int REQUEST_IMAGE_PICK = 1;
    private ImageView imagePreview;
    private View imageCard;

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
        database = HabitTrackerDatabase.getInstance(requireContext());
        // Use the new transparent dialog theme
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_journal, null);

        EditText journalEditText = view.findViewById(R.id.journal_edit_text);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        imagePreview = view.findViewById(R.id.journal_image_preview);
        imageCard = view.findViewById(R.id.journal_image_card);
        Button btnAddImage = view.findViewById(R.id.btn_add_image);

        btnAddImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });

        if (journalItem != null) {
            // Edit mode
            dialogTitle.setText("Edit Journal Entry");
            journalEditText.setText(journalItem.content);
            journalImagePath = journalItem.photoPath;
            if (!TextUtils.isEmpty(journalImagePath)) {
                File file = new File(journalImagePath);
                if (file.exists()) {
                    imageCard.setVisibility(View.VISIBLE);
                    Glide.with(this).load(file).into(imagePreview);
                }
            }
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
                database.updateJournalEntry(journalItem.id, content, journalImagePath);
                Toast.makeText(getContext(), "Journal entry updated", Toast.LENGTH_SHORT).show();
            } else {
                // Add new item
                database.insertJournalEntry(date, content, journalImagePath);
                Toast.makeText(getContext(), "Journal entry added", Toast.LENGTH_SHORT).show();
            }

            if (refreshListener != null) {
                refreshListener.onRefresh();
            }
            dismiss();
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dismiss());

        AlertDialog dialog = builder.setView(view).create();
        
        // Fix dialog width to match other dialogs
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }

        return dialog;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == android.app.Activity.RESULT_OK && requestCode == REQUEST_IMAGE_PICK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            saveImageToInternalStorage(imageUri);
        }
    }

    private void saveImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            File imagesDir = new File(requireContext().getFilesDir(), "images");
            if (!imagesDir.exists()) imagesDir.mkdirs();
            
            File file = new File(imagesDir, "journal_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            
            journalImagePath = file.getAbsolutePath();
            imageCard.setVisibility(View.VISIBLE);
            Glide.with(this).load(file).into(imagePreview);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() != null) {
            getActivity().getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }
}
