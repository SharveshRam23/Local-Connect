package com.example.localconnect.ui.provider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.ServiceProvider;
import com.google.android.material.textfield.TextInputEditText;

@dagger.hilt.android.AndroidEntryPoint
public class ProviderRegistrationActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone, etPincode, etPassword, etExperience;
    private Spinner spinnerCategory;
    private Button btnRegister;

    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @javax.inject.Inject
    com.example.localconnect.data.dao.ProviderDao providerDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_registration);

        etName = findViewById(R.id.etProviderName);
        etPhone = findViewById(R.id.etProviderPhone);
        etPincode = findViewById(R.id.etProviderPincode);
        etPassword = findViewById(R.id.etProviderPassword);
        etExperience = findViewById(R.id.etProviderExperience);
        spinnerCategory = findViewById(R.id.spinnerProviderCategory);
        btnRegister = findViewById(R.id.btnProviderRegister);

        setupSpinner();

        btnRegister.setOnClickListener(v -> registerProvider());
    }

    private void setupSpinner() {
        String[] categories = { "Plumber", "Electrician", "Carpenter", "Maid", "Tutor", "Other" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void registerProvider() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String pincode = etPincode.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String experience = etExperience != null ? etExperience.getText().toString().trim() : "0";

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(pincode)
                || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        ServiceProvider provider = new ServiceProvider(name, category, pincode, phone, password, experience);

        // First, check if provider exists in Firestore
        firestore.collection("service_providers")
                .whereEqualTo("phone", phone)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "Provider already registered with this phone", Toast.LENGTH_SHORT).show();
                    } else {
                        // Save to Firestore
                        firestore.collection("service_providers")
                                .document(provider.id)
                                .set(provider)
                                .addOnSuccessListener(aVoid -> {
                                    // Also save to local Room for sync
                                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                                        providerDao.insert(provider);
                                        runOnUiThread(() -> {
                                            Toast.makeText(this, "Registration Successful. Waiting for Admin Approval.", Toast.LENGTH_SHORT).show();
                                            finish();
                                        });
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
