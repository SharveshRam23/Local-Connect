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
        String phone = binding.etLoginPhone.getText().toString().trim();
        String password = binding.etLoginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                User user = userDao.login(phone, password);
                runOnUiThread(() -> {
                    if (user != null) {
                        // Save Session
                        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
                        prefs.edit()
                                .putString("user_pincode", user.pincode)
                                .putString("user_name", user.name)
                                .putString("user_phone", user.phone)
                                .putInt("user_id", user.id)
                                .putBoolean("is_user_login", true)
                                .apply();

                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UserLoginActivity.this, UserHomeActivity.class);
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
