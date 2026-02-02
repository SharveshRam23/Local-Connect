package com.example.localconnect.ui.user;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.User;
import com.example.localconnect.util.LocationHelper;
import com.google.android.material.textfield.TextInputEditText;

public class UserRegistrationActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone, etPincode, etPassword;
    private Button btnDetectLocation, btnRegister;
    private LocationHelper locationHelper;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etPincode = findViewById(R.id.etPincode);
        etPassword = findViewById(R.id.etPassword);
        btnDetectLocation = findViewById(R.id.btnDetectLocation);
        btnRegister = findViewById(R.id.btnRegister);

        locationHelper = new LocationHelper(this);

        btnDetectLocation.setOnClickListener(v -> checkAndRequestLocationPermission());

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void checkAndRequestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            detectLocation();
        }
    }

    private void detectLocation() {
        Toast.makeText(this, "Detecting location...", Toast.LENGTH_SHORT).show();
        locationHelper.getCurrentPincode(this, new LocationHelper.LocationResultListener() {
            @Override
            public void onLocationFound(String pincode) {
                etPincode.setText(pincode);
                Toast.makeText(UserRegistrationActivity.this, "Location detected", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(UserRegistrationActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                detectLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String pincode = etPincode.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(pincode)
                || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = new User(name, phone, pincode, password);
            AppDatabase.getDatabase(getApplicationContext()).userDao().insert(user);
            runOnUiThread(() -> {
                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();

                // Save user info to SharedPreferences
                SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
                prefs.edit()
                        .putString("user_pincode", pincode)
                        .putString("user_name", name)
                        .putBoolean("is_user_login", true)
                        .apply();

                Intent intent = new Intent(UserRegistrationActivity.this, UserHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }
}
