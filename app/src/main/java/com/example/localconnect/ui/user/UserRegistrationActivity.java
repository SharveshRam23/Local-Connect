package com.example.localconnect.ui.user;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.localconnect.databinding.ActivityUserRegistrationBinding;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.User;
import com.example.localconnect.util.LocationHelper;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UserRegistrationActivity extends AppCompatActivity {

    private ActivityUserRegistrationBinding binding;
    private LocationHelper locationHelper;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @javax.inject.Inject
    com.example.localconnect.data.dao.UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        locationHelper = new LocationHelper(this);

        binding.btnDetectLocation.setOnClickListener(v -> checkAndRequestLocationPermission());
        binding.btnRegister.setOnClickListener(v -> registerUser());
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
                binding.etPincode.setText(pincode);
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
        String name = binding.etName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String pincode = binding.etPincode.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(pincode)
                || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = new User(name, phone, pincode, password);
            userDao.insert(user);
            runOnUiThread(() -> {
                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();

                // Save user info to SharedPreferences
                SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
                prefs.edit()
                        .putString("user_pincode", pincode)
                        .putString("user_name", name)
                        .putString("user_phone", phone)
                        .putString("user_id", user.id)
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
