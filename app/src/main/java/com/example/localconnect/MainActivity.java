package com.example.localconnect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.localconnect.ui.admin.AdminDashboardActivity;
import com.example.localconnect.ui.admin.AdminLoginActivity;
import com.example.localconnect.ui.provider.ProviderDashboardActivity;
import com.example.localconnect.ui.user.UserHomeActivity;
import com.example.localconnect.ui.user.UserLoginActivity;
import com.example.localconnect.worker.NoticeWorker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is returning from dashboard (don't auto-redirect)
        boolean fromDashboard = getIntent().getBooleanExtra("from_dashboard", false);

        // Session Check - only auto-redirect if NOT coming from dashboard
        if (!fromDashboard) {
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
        }

        setContentView(R.layout.activity_main);

        // Card-based navigation
        CardView cardUser = findViewById(R.id.cardUser);
        CardView cardProvider = findViewById(R.id.cardProvider);
        CardView cardAdmin = findViewById(R.id.cardAdmin);

        cardUser.setOnClickListener(v -> startActivity(new Intent(this, UserLoginActivity.class)));
        cardProvider.setOnClickListener(v -> startActivity(new Intent(this, com.example.localconnect.ui.provider.ProviderLoginActivity.class)));
        cardAdmin.setOnClickListener(v -> startActivity(new Intent(this, AdminLoginActivity.class)));

        // Schedule Worker (only once)
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        if (!prefs.getBoolean("worker_scheduled", false)) {
            PeriodicWorkRequest noticeWorkRequest = new PeriodicWorkRequest.Builder(NoticeWorker.class, 15, TimeUnit.MINUTES)
                    .build();
            WorkManager.getInstance(this).enqueue(noticeWorkRequest);
            prefs.edit().putBoolean("worker_scheduled", true).apply();
        }
    }
}