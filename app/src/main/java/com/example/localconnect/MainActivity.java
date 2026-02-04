package com.example.localconnect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.localconnect.ui.admin.AdminDashboardActivity;
import com.example.localconnect.ui.admin.AdminLoginActivity;
import com.example.localconnect.ui.provider.ProviderDashboardActivity;
import com.example.localconnect.ui.provider.ProviderRegistrationActivity;
import com.example.localconnect.ui.user.UserHomeActivity;
import com.example.localconnect.ui.user.UserLoginActivity;
import com.example.localconnect.worker.NoticeWorker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Session Check
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        if (prefs.getBoolean("is_user_login", false)) {
            startActivity(new Intent(this, UserHomeActivity.class));
            finish();
            return;
        } else if (prefs.getBoolean("is_admin_login", false)) {
            startActivity(new Intent(this, AdminDashboardActivity.class));
            finish();
            return;
        } else if (prefs.getBoolean("is_provider_login", false)) {
            startActivity(new Intent(this, ProviderDashboardActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        Button btnUserMode = findViewById(R.id.btnUserMode);
        Button btnProviderMode = findViewById(R.id.btnProviderMode);
        Button btnAdminMode = findViewById(R.id.btnAdminMode);

        btnUserMode.setOnClickListener(v -> startActivity(new Intent(this, UserLoginActivity.class)));
        btnProviderMode.setOnClickListener(
                v -> startActivity(new Intent(this, com.example.localconnect.ui.provider.ProviderLoginActivity.class)));
        btnAdminMode.setOnClickListener(v -> startActivity(new Intent(this, AdminLoginActivity.class)));

        // Schedule Worker
        PeriodicWorkRequest noticeWorkRequest = new PeriodicWorkRequest.Builder(NoticeWorker.class, 15,
                TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(this).enqueue(noticeWorkRequest);
    }
}