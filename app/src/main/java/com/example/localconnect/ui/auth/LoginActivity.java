package com.example.localconnect.ui.auth;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.localconnect.MainActivity;
import com.example.localconnect.R;
import com.example.localconnect.ui.admin.AdminHomeActivity;
import com.example.localconnect.model.User;
import com.example.localconnect.viewmodel.UserViewModel;

public class LoginActivity extends AppCompatActivity {

    private EditText etLoginEmail, etLoginPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etLoginEmail.getText()) || TextUtils.isEmpty(etLoginPassword.getText())) {
                    Toast.makeText(LoginActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                } else {
                    String email = etLoginEmail.getText().toString();
                    String password = etLoginPassword.getText().toString();
                    new GetUserAsyncTask().execute(email, password);
                }
            }
        });

        tvGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }
        });
    }

    private class GetUserAsyncTask extends AsyncTask<String, Void, User> {
        @Override
        protected User doInBackground(String... strings) {
            return userViewModel.getUser(strings[0], strings[1]);
        }

        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);
            if (user != null) {
                Intent intent;
                if (user.getRole().equals("admin")) {
                    intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, MainActivity.class);
                }
                intent.putExtra("user_name", user.getName());
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
