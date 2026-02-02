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

    private TextInputEditText etName, etPhone, etPincode, etPassword;
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

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(pincode)
                || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            ServiceProvider provider = new ServiceProvider(name, category, pincode, phone, password);
            AppDatabase.getDatabase(getApplicationContext()).providerDao().insert(provider);
            runOnUiThread(() -> {
                Toast.makeText(this, "Registration Successful. Waiting for Admin Approval.", Toast.LENGTH_LONG).show();

                SharedPreferences prefs = getSharedPreferences("local_connect_prefs", Context.MODE_PRIVATE);
                prefs.edit()
                        .putString("provider_pincode", pincode)
                        .putString("provider_name", name)
                        .putBoolean("is_provider_login", true)
                        .apply();

                Intent intent = new Intent(ProviderRegistrationActivity.this, ProviderDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }
}
