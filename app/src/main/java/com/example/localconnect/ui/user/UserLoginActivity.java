package com.example.localconnect.ui.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.User;
import com.google.android.material.textfield.TextInputEditText;

public class UserLoginActivity extends AppCompatActivity {

    private TextInputEditText etPhone, etPassword;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login); // Need to create layout

        etPhone = findViewById(R.id.etLoginPhone);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnGoToRegister);

        btnLogin.setOnClickListener(v -> loginUser());
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, UserRegistrationActivity.class));
            finish();
        });
    }

    private void loginUser() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = AppDatabase.getDatabase(getApplicationContext()).userDao().login(phone, password);
            runOnUiThread(() -> {
                if (user != null) {
                    // Save Session
                    SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("user_pincode", user.pincode)
                            .putString("user_name", user.name)
                            .putString("user_phone", user.phone)
                            .putBoolean("is_user_login", true)
                            .apply();

                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UserLoginActivity.this, UserHomeActivity.class);
                    // Clear back stack so user can't go back to login
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
