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

@dagger.hilt.android.AndroidEntryPoint
public class ProviderLoginActivity extends AppCompatActivity {

    private EditText etPhone, etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    @javax.inject.Inject
    com.example.localconnect.data.dao.ProviderDao providerDao;

    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_login);

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
        String phoneInput = etPhone.getText().toString().trim();
        String passwordInput = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(phoneInput) || TextUtils.isEmpty(passwordInput)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Search in Firestore
        firestore.collection("service_providers")
                .whereEqualTo("phone", phoneInput)
                .whereEqualTo("password", passwordInput)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        ServiceProvider provider = queryDocumentSnapshots.getDocuments().get(0).toObject(ServiceProvider.class);
                        if (provider != null) {
                            // Sync to Room
                            com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                                providerDao.insert(provider);
                                runOnUiThread(() -> {
                                    handleLoginSuccess(provider);
                                });
                            });
                        }
                    } else {
                        // Fallback to local Room
                        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                            ServiceProvider localProvider = providerDao.checkLogin(phoneInput, passwordInput);
                            runOnUiThread(() -> {
                                if (localProvider != null) {
                                    handleLoginSuccess(localProvider);
                                } else {
                                    Toast.makeText(this, "Invalid Phone or Password", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    // Firestore failed, try local
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        ServiceProvider localProvider = providerDao.checkLogin(phoneInput, passwordInput);
                        runOnUiThread(() -> {
                            if (localProvider != null) {
                                handleLoginSuccess(localProvider);
                            } else {
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                });
    }

    private void handleLoginSuccess(ServiceProvider provider) {
        if (provider.isApproved) {
            // Login Success
            com.example.localconnect.util.SessionManager sessionManager = new com.example.localconnect.util.SessionManager(this);
            sessionManager.createProviderSession(provider.id, provider.name, provider.pincode, provider.profileImageUrl);

            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProviderLoginActivity.this, ProviderDashboardActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Account pending approval. Please wait for Admin.", Toast.LENGTH_LONG)
                    .show();
        }
    }
}
