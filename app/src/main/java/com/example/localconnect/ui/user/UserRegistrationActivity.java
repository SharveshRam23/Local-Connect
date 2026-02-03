package com.example.localconnect.ui.user;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.localconnect.databinding.ActivityUserRegistrationBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UserRegistrationActivity extends AppCompatActivity {

    private ActivityUserRegistrationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnRegister.setOnClickListener(v -> {
            // For now, we'll just navigate to HomeActivity
            startActivity(new Intent(UserRegistrationActivity.this, HomeActivity.class));
        });
    }
}