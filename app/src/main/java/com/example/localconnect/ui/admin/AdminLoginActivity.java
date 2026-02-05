package com.example.localconnect.ui.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.localconnect.databinding.ActivityAdminLoginBinding;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.User;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AdminLoginActivity extends AppCompatActivity {

    private ActivityAdminLoginBinding binding;

    @javax.inject.Inject
    com.example.localconnect.data.dao.UserDao userDao;

    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnAdminLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String usernameInput = binding.etAdminUsername.getText().toString().trim();
        String passwordInput = binding.etAdminPassword.getText().toString().trim();

        if (TextUtils.isEmpty(usernameInput) || TextUtils.isEmpty(passwordInput)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Search in Firestore
        firestore.collection("users")
                .whereEqualTo("phone", usernameInput)
                .whereEqualTo("password", passwordInput)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        User admin = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                        if (admin != null) {
                            // Sync to Room
                            com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                                userDao.insert(admin);
                                runOnUiThread(() -> handleLoginSuccess(admin));
                            });
                        }
                    } else {
                        // Not in Firestore, check Room
                        checkLocalAndRepair(usernameInput, passwordInput);
                    }
                })
                .addOnFailureListener(e -> {
                    // Firestore failed, check Room
                    checkLocalAndRepair(usernameInput, passwordInput);
                });
    }

    private void checkLocalAndRepair(String username, String password) {
        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
            User localAdmin = userDao.login(username, password);
            if (localAdmin != null) {
                runOnUiThread(() -> handleLoginSuccess(localAdmin));
            } else {
                // Check if it's the default admin credentials and needs repair
                if ("admin".equals(username) && "admin123".equals(password)) {
                    repairAdminAccount();
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void repairAdminAccount() {
        // Create new admin user object
        User admin = new User("admin_fixed_id", "Admin", "admin", "000000", "admin123");
        
        // Save to Firestore first to ensure cloud sync
        firestore.collection("users")
                .document(admin.id)
                .set(admin)
                .addOnSuccessListener(aVoid -> {
                    // Sync to Room
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        userDao.insert(admin);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Admin account repaired and synced!", Toast.LENGTH_SHORT).show();
                            handleLoginSuccess(admin);
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    // Even if Firestore fails, allow local login for repair
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        userDao.insert(admin);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Admin account repaired locally (Cloud failed)", Toast.LENGTH_SHORT).show();
                            handleLoginSuccess(admin);
                        });
                    });
                });
    }

    private void handleLoginSuccess(User admin) {
        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("is_admin_login", true)
                .putString("user_name", admin.name)
                .apply();

        Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
