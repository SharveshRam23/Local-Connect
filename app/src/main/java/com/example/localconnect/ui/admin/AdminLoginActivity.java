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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnAdminLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String username = binding.etAdminUsername.getText().toString().trim();
        String password = binding.etAdminPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                User admin = userDao.login(username, password);
                runOnUiThread(() -> {
                    if (admin != null) {
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

                        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
                        prefs.edit()
                                .putBoolean("is_admin_login", true)
                                .putString("user_name", admin.name)
                                .apply();

                        Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}
