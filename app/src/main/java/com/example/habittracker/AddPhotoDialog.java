package com.example.habittracker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AddPhotoDialog extends DialogFragment {
    private static final int REQUEST_IMAGE_PICK = 1;
    private String date;
    private HabitTrackerDatabase database;
    private EditText editCaption;
    private String photoPath = "";
    private RefreshListener refreshListener;

    public void setDate(String date) {
        this.date = date;
    }

    public void setRefreshListener(RefreshListener listener) {
        this.refreshListener = listener;
    }

    @NonNull
    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        database = HabitTrackerDatabase.getInstance(requireContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        android.view.LayoutInflater inflater = requireActivity().getLayoutInflater();
        android.view.View view = inflater.inflate(R.layout.dialog_add_photo, null);
        
        editCaption = view.findViewById(R.id.edit_caption);
        
        builder.setView(view)
                .setTitle("Add Photo")
                .setPositiveButton("Choose Photo", (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, REQUEST_IMAGE_PICK);
                })
                .setNegativeButton("Cancel", null);
        
        return builder.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                // Save image to app directory
                File imagesDir = new File(requireContext().getFilesDir(), "images");
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs();
                }
                File imageFile = new File(imagesDir, "photo_" + System.currentTimeMillis() + ".jpg");
                
                InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
                FileOutputStream outputStream = new FileOutputStream(imageFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.close();
                inputStream.close();
                
                photoPath = imageFile.getAbsolutePath();
                String caption = editCaption.getText().toString().trim();
                database.insertPhoto(date, photoPath, caption);
                Toast.makeText(requireContext(), "Photo added", Toast.LENGTH_SHORT).show();
                if (refreshListener != null) {
                    refreshListener.onRefresh();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Error saving photo", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
