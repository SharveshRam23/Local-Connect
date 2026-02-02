package com.example.localconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.localconnect.ui.auth.LoginActivity;
import com.example.localconnect.ui.issue.ReportIssueActivity;
import com.example.localconnect.ui.admin.AdminHomeActivity;
import com.example.localconnect.ui.user.HomeActivity;
import com.example.localconnect.worker.NoticeWorker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        Button btnFindServices = findViewById(R.id.btnFindServices);
        Button btnReportIssue = findViewById(R.id.btnReportIssue);
        Button btnProviderDashboard = findViewById(R.id.btnProviderDashboard);
        Button btnLogout = findViewById(R.id.btnLogout);

        String userName = getIntent().getStringExtra("user_name");
        if (userName != null) {
            tvWelcome.setText("Welcome, " + userName);
        }

        btnFindServices.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        btnReportIssue.setOnClickListener(v -> startActivity(new Intent(this, ReportIssueActivity.class)));
        btnProviderDashboard.setOnClickListener(v -> startActivity(new Intent(this, AdminHomeActivity.class)));

        btnLogout.setOnClickListener(v -> {
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