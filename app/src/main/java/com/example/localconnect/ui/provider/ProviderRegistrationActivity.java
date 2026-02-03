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

public class ProviderRegistrationActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone, etPincode, etPassword, etExperience;
    private Spinner spinnerCategory;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_registration);

        etName = findViewById(R.id.etProviderName);
        etPhone = findViewById(R.id.etProviderPhone);
        etPincode = findViewById(R.id.etProviderPincode);
        etPassword = findViewById(R.id.etProviderPassword);
        // Assuming the layout XML will be updated to include this ID.
        // It is safer to assume I need to update XML too, but the user requested Java
        // code.
        // I will assume the XML has R.id.etProviderExperience or I need to add it.
        // The user request demanded "Full Java code ... XML layouts".
        // I haven't updated XML yet. I should do that.
        // For now, I'll use the ID `etProviderExperience` and ensure I update XML next.
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

        AppDatabase.databaseWriteExecutor.execute(() -> {
            ServiceProvider provider = new ServiceProvider(name, category, pincode, phone, password, experience);
            AppDatabase.getDatabase(getApplicationContext()).providerDao().insert(provider);
            runOnUiThread(() -> {
                Toast.makeText(this, "Registration Successful. Waiting for Admin Approval.", Toast.LENGTH_SHORT).show();
                finish(); // Close registration, return to previous screen (likely Login or Main)
            });
        });
    }
}
