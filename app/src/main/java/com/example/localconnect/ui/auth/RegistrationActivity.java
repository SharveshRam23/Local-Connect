package com.example.localconnect.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.localconnect.R;
import com.example.localconnect.model.User;
import com.example.localconnect.viewmodel.UserViewModel;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etRegisterName, etRegisterEmail, etRegisterPassword, etServiceType, etArea;
    private Button btnRegister;
    private TextView tvGoToLogin;
    private UserViewModel userViewModel;
    private RadioGroup rgRoles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        etRegisterName = findViewById(R.id.etRegisterName);
        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        etServiceType = findViewById(R.id.etServiceType);
        etArea = findViewById(R.id.etArea);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);
        rgRoles = findViewById(R.id.rgRoles);

        rgRoles.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbServiceProvider) {
                    etServiceType.setVisibility(View.VISIBLE);
                } else {
                    etServiceType.setVisibility(View.GONE);
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etRegisterName.getText()) || TextUtils.isEmpty(etRegisterEmail.getText()) || TextUtils.isEmpty(etRegisterPassword.getText()) || TextUtils.isEmpty(etArea.getText())) {
                    Toast.makeText(RegistrationActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                } else {
                    String name = etRegisterName.getText().toString();
                    String email = etRegisterEmail.getText().toString();
                    String password = etRegisterPassword.getText().toString();
                    String area = etArea.getText().toString();
                    String role = "user";
                    int checkedId = rgRoles.getCheckedRadioButtonId();
                    if (checkedId == R.id.rbServiceProvider) {
                        role = "service";
                    }
                    User user = new User(name, email, password, role, area);
                    userViewModel.insert(user);
                    Toast.makeText(RegistrationActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                    finish();
                }
            }
        });

        tvGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            }
        });
    }
}
