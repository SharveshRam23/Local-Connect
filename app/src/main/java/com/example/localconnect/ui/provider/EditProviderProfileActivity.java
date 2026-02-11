package com.example.localconnect.ui.provider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.localconnect.databinding.ActivityEditProviderProfileBinding;
import com.example.localconnect.model.ServiceProvider;
import com.example.localconnect.ui.issue.ImageEditorActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditProviderProfileActivity extends AppCompatActivity {

    private ActivityEditProviderProfileBinding binding;
    private String providerId;
    private ServiceProvider currentProvider;
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
                        if (currentProvider != null) {
                            currentProvider.profileImageUrl = uriString;
                        }
                    }
                }
            });

    @Inject
    com.example.localconnect.data.dao.ProviderDao providerDao;

    @Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProviderProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        providerId = prefs.getString("provider_id", "");

        if (providerId.isEmpty()) {
            finish();
            return;
        }

        loadProviderData();

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

    private void loadProviderData() {
        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
            currentProvider = providerDao.getProviderById(providerId);
            if (currentProvider != null) {
                runOnUiThread(this::populateUI);
            }
        });

        firestore.collection("service_providers").document(providerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    ServiceProvider provider = documentSnapshot.toObject(ServiceProvider.class);
                    if (provider != null) {
                        currentProvider = provider;
                        populateUI();
                        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> providerDao.insert(provider));
                    }
                });
    }

    private void populateUI() {
        if (currentProvider == null) return;
        binding.etName.setText(currentProvider.name);
        binding.etBio.setText(currentProvider.bio != null ? currentProvider.bio : "");
        if (currentProvider.profileImageUrl != null && !currentProvider.profileImageUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                    .load(currentProvider.profileImageUrl)
                    .placeholder(com.example.localconnect.R.drawable.ic_profile)
                    .into(binding.ivProfileImage);
        }
        binding.etCategory.setText(currentProvider.category);
        binding.etExperience.setText(currentProvider.experience);
        binding.etAddress.setText(currentProvider.address != null ? currentProvider.address : "");
        binding.etPhone.setText(currentProvider.phone);
        binding.etPincode.setText(currentProvider.pincode);
    }

    private void uploadImageAndSaveProfile() {
        binding.btnSave.setEnabled(false);
        binding.btnSave.setText("Processing...");

        try {
            android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            String base64Image = com.example.localconnect.util.ImageUtil.toBase64Aggressive(bitmap);
            
            if (currentProvider != null) {
                currentProvider.profileImageUrl = "data:image/jpeg;base64," + base64Image;
            }
            saveProfile();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to process image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            binding.btnSave.setEnabled(true);
            binding.btnSave.setText("Save Changes");
        }
    }

    private void saveProfile() {
        if (currentProvider == null) return;

        currentProvider.name = binding.etName.getText().toString().trim();
        currentProvider.bio = binding.etBio.getText().toString().trim();
        currentProvider.category = binding.etCategory.getText().toString().trim();
        currentProvider.experience = binding.etExperience.getText().toString().trim();
        currentProvider.address = binding.etAddress.getText().toString().trim();
        currentProvider.phone = binding.etPhone.getText().toString().trim();
        currentProvider.pincode = binding.etPincode.getText().toString().trim();

        if (currentProvider.name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            binding.btnSave.setEnabled(true);
            binding.btnSave.setText("Save Changes");
            return;
        }

        binding.btnSave.setEnabled(false);
        binding.btnSave.setText("Saving...");

        firestore.collection("service_providers").document(providerId).set(currentProvider)
                .addOnSuccessListener(aVoid -> {
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        providerDao.insert(currentProvider);
                        runOnUiThread(() -> {
                            // Update SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
                            prefs.edit()
                                    .putString("provider_name", currentProvider.name)
                                    .putString("provider_profile_image", currentProvider.profileImageUrl)
                                    .apply();

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
