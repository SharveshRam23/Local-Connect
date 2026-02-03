package com.example.localconnect.ui.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.localconnect.databinding.ActivityAdminHomeBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AdminHomeActivity extends AppCompatActivity {

    private ActivityAdminHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnViewPendingRequests.setOnClickListener(v -> 
            startActivity(new Intent(AdminHomeActivity.this, ViewPendingRequestsActivity.class))
        );

        binding.btnAddNotice.setOnClickListener(v -> 
            startActivity(new Intent(AdminHomeActivity.this, AddNoticeActivity.class))
        );

        binding.btnViewIssues.setOnClickListener(v -> 
            startActivity(new Intent(AdminHomeActivity.this, ViewIssuesActivity.class))
        );
    }
}
