package com.example.localconnect.ui.provider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.ServiceProvider;

public class ProviderLoginActivity extends AppCompatActivity {

    private EditText etPhone, etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_login); // Need to create this layout

        etPhone = findViewById(R.id.etProviderLoginPhone);
        etPassword = findViewById(R.id.etProviderLoginPassword);
        btnLogin = findViewById(R.id.btnProviderLogin);
        tvRegister = findViewById(R.id.tvProviderRegisterLink);

        btnLogin.setOnClickListener(v -> loginProvider());
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, ProviderRegistrationActivity.class));
        });
    }

    private void loginProvider() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            ServiceProvider provider = AppDatabase.getDatabase(getApplicationContext())
                    .providerDao().checkLogin(phone, password);

            runOnUiThread(() -> {
                if (provider != null) {
                    if (provider.isApproved) {
                        // Login Success
                        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", Context.MODE_PRIVATE);
                        prefs.edit()
                                .putBoolean("is_provider_login", true)
                                .putString("provider_name", provider.name)
                                .putString("provider_pincode", provider.pincode)
                                .putInt("provider_id", provider.id)
                                .apply();

                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ProviderLoginActivity.this, ProviderDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Account pending approval. Please wait for Admin.", Toast.LENGTH_LONG)
                                .show();
                    }
                } else {
                    Toast.makeText(this, "Invalid Phone or Password", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
