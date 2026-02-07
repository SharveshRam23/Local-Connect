package com.example.localconnect.ui.admin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.data.dao.UserDao;
import com.example.localconnect.databinding.ActivityAdminProfileBinding;
import com.example.localconnect.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AdminProfileActivity extends AppCompatActivity {

    private ActivityAdminProfileBinding binding;
    private User adminUser;

    @Inject
    UserDao userDao;

    @Inject
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadAdminProfile();

        binding.btnSaveAdminProfile.setOnClickListener(v -> saveAdminProfile());
    }

    private void loadAdminProfile() {
        // We assume the admin is logged in and their phone is in preferences
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        String name = prefs.getString("user_name", "Admin");

        // Try to fetch by name/phone or the fixed admin ID
        firestore.collection("users")
                .document("admin_fixed_id")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    adminUser = documentSnapshot.toObject(User.class);
                    if (adminUser != null) {
                        displayProfile();
                    } else {
                        // Try fetching by phone if fixed ID fails
                        fetchByPhone(name);
                    }
                })
                .addOnFailureListener(e -> fetchByPhone(name));
    }

    private void fetchByPhone(String phone) {
        firestore.collection("users")
                .whereEqualTo("phone", phone)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        adminUser = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                        displayProfile();
                    }
                });
    }

    private void displayProfile() {
        if (adminUser != null) {
            binding.etAdminName.setText(adminUser.name);
            binding.etAdminPincode.setText(adminUser.pincode);
            binding.etAdminPassword.setText(adminUser.password);
        }
    }

    private void saveAdminProfile() {
        if (adminUser == null) return;

        String name = binding.etAdminName.getText().toString().trim();
        String pincode = binding.etAdminPincode.getText().toString().trim();
        String password = binding.etAdminPassword.getText().toString().trim();

        if (name.isEmpty() || pincode.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        adminUser.name = name;
        adminUser.pincode = pincode;
        adminUser.password = password;

        firestore.collection("users")
                .document(adminUser.id)
                .set(adminUser)
                .addOnSuccessListener(aVoid -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        userDao.insert(adminUser); // REPLACE strategy
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                            
                            // Update name in prefs
                            getSharedPreferences("local_connect_prefs", MODE_PRIVATE)
                                    .edit().putString("user_name", adminUser.phone).apply();
                        });
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
