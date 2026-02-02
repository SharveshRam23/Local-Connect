package com.example.localconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.localconnect.ui.admin.AdminLoginActivity;
import com.example.localconnect.ui.provider.ProviderRegistrationActivity;
import com.example.localconnect.ui.user.UserLoginActivity;
import com.example.localconnect.worker.NoticeWorker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnUserMode = findViewById(R.id.btnUserMode);
        Button btnProviderMode = findViewById(R.id.btnProviderMode);
        Button btnAdminMode = findViewById(R.id.btnAdminMode);

        btnUserMode.setOnClickListener(v -> startActivity(new Intent(this, UserLoginActivity.class)));
        btnProviderMode.setOnClickListener(v -> startActivity(new Intent(this, ProviderRegistrationActivity.class)));
        btnAdminMode.setOnClickListener(v -> startActivity(new Intent(this, AdminLoginActivity.class)));

        // Schedule Worker
        PeriodicWorkRequest noticeWorkRequest = new PeriodicWorkRequest.Builder(NoticeWorker.class, 15,
                TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(this).enqueue(noticeWorkRequest);
    }
}