package com.example.localconnect.ui.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.localconnect.databinding.ActivityUserLoginBinding;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.User;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UserLoginActivity extends AppCompatActivity {

    private ActivityUserLoginBinding binding;

    @javax.inject.Inject
    com.example.localconnect.data.dao.UserDao userDao;

    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogin.setOnClickListener(v -> loginUser());
        binding.btnGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, UserRegistrationActivity.class));
            finish();
        });
    }

    private void loginUser() {
        String phoneInput = binding.etLoginPhone.getText().toString().trim();
        String passwordInput = binding.etLoginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(phoneInput) || TextUtils.isEmpty(passwordInput)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Search in Firestore
        firestore.collection("users")
                .whereEqualTo("phone", phoneInput)
                .whereEqualTo("password", passwordInput)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        User user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                        if (user != null) {
                            // Sync to Room
                            com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                                userDao.insert(user);
                                runOnUiThread(() -> {
                                    handleLoginSuccess(user);
                                });
                            });
                        }
                    } else {
                        // Fallback to local Room (just in case)
                        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                            User localUser = userDao.login(phoneInput, passwordInput);
                            runOnUiThread(() -> {
                                if (localUser != null) {
                                    handleLoginSuccess(localUser);
                                } else {
                                    Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    // Firestore failed, try local
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        User localUser = userDao.login(phoneInput, passwordInput);
                        runOnUiThread(() -> {
                            if (localUser != null) {
                                handleLoginSuccess(localUser);
                            } else {
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                });
    }

    private void handleLoginSuccess(User user) {
        // Save Session
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        prefs.edit()
                .putString("user_pincode", user.pincode)
                .putString("user_name", user.name)
                .putString("user_phone", user.phone)
                .putString("user_id", user.id)
                .putString("user_profile_image", user.profileImageUrl)
                .putBoolean("is_user_login", true)
                .apply();

        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(UserLoginActivity.this, UserHomeActivity.class);
        startActivity(intent);
        finish();
    }
}
