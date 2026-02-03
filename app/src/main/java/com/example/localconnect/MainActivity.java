package com.example.localconnect;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.localconnect.databinding.ActivityMainBinding;
import com.example.localconnect.ui.auth.LoginActivity;
import com.example.localconnect.ui.issue.ReportIssueActivity;
import com.example.localconnect.ui.admin.AdminHomeActivity;
import com.example.localconnect.ui.user.HomeActivity;
import com.example.localconnect.worker.NoticeWorker;

import java.util.concurrent.TimeUnit;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String userName = getIntent().getStringExtra("user_name");
        if (userName != null) {
            binding.tvWelcome.setText("Welcome, " + userName);
        }

        binding.btnFindServices.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        binding.btnReportIssue.setOnClickListener(v -> startActivity(new Intent(this, ReportIssueActivity.class)));
        binding.btnProviderDashboard.setOnClickListener(v -> startActivity(new Intent(this, AdminHomeActivity.class)));

        binding.btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Schedule Worker
        PeriodicWorkRequest noticeWorkRequest = new PeriodicWorkRequest.Builder(NoticeWorker.class, 15,
                TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(this).enqueue(noticeWorkRequest);
    }
}
