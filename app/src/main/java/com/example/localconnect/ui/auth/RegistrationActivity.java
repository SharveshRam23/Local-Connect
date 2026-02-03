package com.example.localconnect.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.localconnect.databinding.ActivityRegistrationBinding;
import com.example.localconnect.model.User;
import com.example.localconnect.viewmodel.UserViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegistrationActivity extends AppCompatActivity {

    private ActivityRegistrationBinding binding;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        binding.rgRoles.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.rbServiceProvider.getId()) {
                binding.etServiceType.setVisibility(View.VISIBLE);
            } else {
                binding.etServiceType.setVisibility(View.GONE);
            }
        });

        binding.btnRegister.setOnClickListener(v -> {
            String name = binding.etRegisterName.getText().toString();
            String email = binding.etRegisterEmail.getText().toString();
            String password = binding.etRegisterPassword.getText().toString();
            String area = binding.etArea.getText().toString();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(area)) {
                Toast.makeText(RegistrationActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else {
                String role = "user";
                int checkedId = binding.rgRoles.getCheckedRadioButtonId();
                if (checkedId == binding.rbServiceProvider.getId()) {
                    role = "service";
                }
                User user = new User(name, email, password, role, area);
                userViewModel.insert(user);
                Toast.makeText(RegistrationActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                finish();
            }
        });

        binding.tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
        });
    }
}
