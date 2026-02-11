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

@dagger.hilt.android.AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("LocalConnect", "MainActivity onCreate started");
        
        // Session Check - auto-redirect if NOT coming from dashboard
        boolean fromDashboard = getIntent().getBooleanExtra("from_dashboard", false);
        if (!fromDashboard) {
            SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
            if (prefs.getBoolean("is_user_login", false)) {
                android.util.Log.d("LocalConnect", "Redirecting to UserHome");
                startActivity(new Intent(this, com.example.localconnect.ui.user.UserHomeActivity.class));
                finish();
                return;
            } else if (prefs.getBoolean("is_admin_login", false)) {
                android.util.Log.d("LocalConnect", "Redirecting to AdminDashboard");
                startActivity(new Intent(this, com.example.localconnect.ui.admin.AdminDashboardActivity.class));
                finish();
                return;
            } else if (prefs.getBoolean("is_provider_login", false)) {
                android.util.Log.d("LocalConnect", "Redirecting to ProviderDashboard");
                startActivity(new Intent(this, com.example.localconnect.ui.provider.ProviderDashboardActivity.class));
                finish();
                return;
            }
        }

        setContentView(R.layout.activity_main);
        
        // Schedule Worker (only once)
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        if (!prefs.getBoolean("worker_scheduled", false)) {
            PeriodicWorkRequest noticeWorkRequest = new PeriodicWorkRequest.Builder(NoticeWorker.class, 15, TimeUnit.MINUTES)
                    .build();
            WorkManager.getInstance(this).enqueue(noticeWorkRequest);
            prefs.edit().putBoolean("worker_scheduled", true).apply();
        }

        // Restore navigation listeners
        findViewById(R.id.cardUser).setOnClickListener(v -> 
            startActivity(new Intent(this, com.example.localconnect.ui.user.UserLoginActivity.class)));
        findViewById(R.id.cardProvider).setOnClickListener(v -> 
            startActivity(new Intent(this, com.example.localconnect.ui.provider.ProviderLoginActivity.class)));
        findViewById(R.id.cardAdmin).setOnClickListener(v -> 
            startActivity(new Intent(this, com.example.localconnect.ui.admin.AdminLoginActivity.class)));

        android.util.Log.d("LocalConnect", "MainActivity onCreate finished");
    }
}