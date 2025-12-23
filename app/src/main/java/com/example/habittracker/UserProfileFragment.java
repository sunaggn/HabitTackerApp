package com.example.habittracker;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UserProfileFragment extends Fragment {
    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    private TextInputEditText editName;
    private TextInputEditText editSurname;
    private TextInputEditText editEmail;
    private TextInputEditText editPhone;
    private ImageView profileImage;
    private Button btnSave;
    private TextView textGender;
    private TextView textBirthdate;
    private HabitTrackerDatabase database;
    private String profileImagePath = "";
    private String selectedGender = "";
    private String selectedBirthdate = "";
    private Uri cameraImageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = new HabitTrackerDatabase(requireContext());
        editName = view.findViewById(R.id.edit_name);
        editSurname = view.findViewById(R.id.edit_surname);
        editEmail = view.findViewById(R.id.edit_email);
        editPhone = view.findViewById(R.id.edit_phone);
        profileImage = view.findViewById(R.id.profile_image);
        btnSave = view.findViewById(R.id.btn_save);
        textGender = view.findViewById(R.id.text_gender);
        textBirthdate = view.findViewById(R.id.text_birthdate);
        android.widget.ImageButton btnBack = view.findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showViewPager();
            }
        });

        loadProfile();

        profileImage.setOnClickListener(v -> showImagePickerOptions());

        view.findViewById(R.id.layout_gender).setOnClickListener(v -> showGenderPicker());
        view.findViewById(R.id.layout_birthdate).setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String surname = editSurname.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();

            database.insertUserProfile(name, surname, email, phone, profileImagePath, selectedGender, selectedBirthdate);
            Toast.makeText(requireContext(), "Profile saved", Toast.LENGTH_SHORT).show();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadUserProfile();
            }
        });
    }

    private void loadProfile() {
        android.database.Cursor cursor = database.getUserProfile();
        if (cursor.moveToFirst()) {
            editName.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            editSurname.setText(cursor.getString(cursor.getColumnIndexOrThrow("surname")));
            editEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            editPhone.setText(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            profileImagePath = cursor.getString(cursor.getColumnIndexOrThrow("profile_image_path"));

            String gender = cursor.getString(cursor.getColumnIndexOrThrow("gender"));
            if (gender != null && !gender.isEmpty()) {
                selectedGender = gender;
                textGender.setText(gender);
            }

            String birthdate = cursor.getString(cursor.getColumnIndexOrThrow("birthdate"));
            if (birthdate != null && !birthdate.isEmpty()) {
                selectedBirthdate = birthdate;
                textBirthdate.setText(birthdate);
            }

            if (!TextUtils.isEmpty(profileImagePath)) {
                File imageFile = new File(profileImagePath);
                if (imageFile.exists()) {
                    profileImage.setImageURI(Uri.fromFile(imageFile));
                }
            }
        }
        cursor.close();
    }

    private void showImagePickerOptions() {
        String[] options = {"Kamera ile Çek", "Galeriden Seç"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Profil Fotoğrafı Seç")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (java.io.IOException ex) {
            // Error occurred while creating the File
            Toast.makeText(requireContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
        }
        if (photoFile != null) {
            cameraImageUri = FileProvider.getUriForFile(requireContext(),
                    "com.example.habittracker.fileprovider",
                    photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private File createImageFile() throws java.io.IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new java.util.Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        profileImagePath = image.getAbsolutePath();
        return image;
    }

    private void showGenderPicker() {
        String[] genders = {"Male", "Female", "Other"};
        int selectedIndex = -1;
        for (int i = 0; i < genders.length; i++) {
            if (genders[i].equals(selectedGender)) {
                selectedIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Gender")
                .setSingleChoiceItems(genders, selectedIndex, (dialog, which) -> {
                    selectedGender = genders[which];
                    textGender.setText(selectedGender);
                    dialog.dismiss();
                })
                .show();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (!selectedBirthdate.isEmpty()) {
            try {
                String[] parts = selectedBirthdate.split("/");
                calendar.set(Calendar.MONTH, Integer.parseInt(parts[0]) - 1);
                calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[1]));
                calendar.set(Calendar.YEAR, Integer.parseInt(parts[2]));
            } catch (Exception e) {
                // Use current date
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedBirthdate = String.format("%02d/%02d/%04d", month + 1, dayOfMonth, year);
                    textBirthdate.setText(selectedBirthdate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null && data.getData() != null) {
                Uri imageUri = data.getData();
                // Save the selected image to the app's internal storage
                saveImageToInternalStorage(imageUri);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // The image is already saved to the path, just update the ImageView
                profileImage.setImageURI(cameraImageUri);
            }
        }
    }

    private void saveImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            File file = createImageFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            profileImagePath = file.getAbsolutePath();
            profileImage.setImageURI(Uri.fromFile(file));
        } catch (java.io.IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }
}
