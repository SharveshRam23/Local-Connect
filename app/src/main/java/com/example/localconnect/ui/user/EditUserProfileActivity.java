package com.example.localconnect.ui.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.localconnect.databinding.ActivityEditUserProfileBinding;
import com.example.localconnect.model.User;
import com.example.localconnect.ui.issue.ImageEditorActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

import android.graphics.Bitmap;
import android.provider.MediaStore;
import java.io.ByteArrayOutputStream;

@AndroidEntryPoint
public class EditUserProfileActivity extends AppCompatActivity {

    private ActivityEditUserProfileBinding binding;
    private String userId;
    private User currentUser;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    launchImageEditor(uri);
                }
            });

    private final ActivityResultLauncher<Intent> editImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String uriString = result.getData().getStringExtra("edited_image_uri");
                    if (uriString != null) {
                        selectedImageUri = Uri.parse(uriString);
                        binding.ivProfileImage.setImageURI(selectedImageUri);
                        if (currentUser != null) {
                            currentUser.profileImageUrl = uriString;
                        }
                    }
                }
            });

    @Inject
    com.example.localconnect.data.dao.UserDao userDao;

    @Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        userId = prefs.getString("user_id", "");

        if (userId.isEmpty()) {
            finish();
            return;
        }

        loadUserData();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadImageAndSaveProfile();
            } else {
                saveProfile();
            }
        });
        binding.ivProfileImage.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });
    }

    private void loadUserData() {
        // Try local first
        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
            currentUser = userDao.getUserById(userId);
            if (currentUser != null) {
                runOnUiThread(this::populateUI);
            }
        });

        // Sync from Firestore
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        currentUser = user;
                        populateUI();
                        // Update local
                        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> userDao.insert(user));
                    }
                });
    }

    private void populateUI() {
        if (currentUser == null) return;
        binding.etName.setText(currentUser.name);
        binding.etBio.setText(currentUser.bio != null ? currentUser.bio : "");
        if (currentUser.profileImageUrl != null && !currentUser.profileImageUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                    .load(currentUser.profileImageUrl)
                    .placeholder(com.example.localconnect.R.drawable.ic_profile)
                    .into(binding.ivProfileImage);
        }
        binding.etPhone.setText(currentUser.phone);
        binding.etPincode.setText(currentUser.pincode);
    }

    private void uploadImageAndSaveProfile() {
        binding.btnSave.setEnabled(false);
        binding.btnSave.setText("Processing...");

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            String base64Image = com.example.localconnect.util.ImageUtil.toBase64Aggressive(bitmap);
            
            if (currentUser != null) {
                currentUser.profileImageUrl = base64Image;
            }
            saveProfile();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to process image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            binding.btnSave.setEnabled(true);
            binding.btnSave.setText("Save Changes");
        }
    }

    private void saveProfile() {
        if (currentUser == null) return;

        currentUser.name = binding.etName.getText().toString().trim();
        currentUser.bio = binding.etBio.getText().toString().trim();
        currentUser.phone = binding.etPhone.getText().toString().trim();
        currentUser.pincode = binding.etPincode.getText().toString().trim();

        if (currentUser.name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            binding.btnSave.setEnabled(true);
            binding.btnSave.setText("Save Changes");
            return;
        }

        binding.btnSave.setEnabled(false);
        binding.btnSave.setText("Saving...");

        // Save to Firestore
        firestore.collection("users").document(userId).set(currentUser)
                .addOnSuccessListener(aVoid -> {
                    // Sync to local
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        userDao.insert(currentUser);
                        runOnUiThread(() -> {
                            // Update SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
                            prefs.edit().putString("user_profile_image", currentUser.profileImageUrl).apply();
                            
                            Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.btnSave.setEnabled(true);
                    binding.btnSave.setText("Save Changes");
                });
    }

    private void launchImageEditor(Uri uri) {
        Intent intent = new Intent(this, ImageEditorActivity.class);
        intent.putExtra("image_uri", uri.toString());
        editImageLauncher.launch(intent);
    }
}
