package com.example.localconnect.ui.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.localconnect.databinding.ActivityUserHomeBinding;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.Notice;
import com.example.localconnect.ui.adapter.NoticeAdapter;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UserHomeActivity extends AppCompatActivity {

    private ActivityUserHomeBinding binding;

    @javax.inject.Inject
    com.example.localconnect.data.dao.NoticeDao noticeDao;

    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.rvNotices.setLayoutManager(new LinearLayoutManager(this));

        binding.btnFindServices.setOnClickListener(v -> {
            startActivity(new Intent(UserHomeActivity.this, ServiceListActivity.class));
        });

        binding.btnViewBookings.setOnClickListener(v -> {
            startActivity(new Intent(UserHomeActivity.this, MyBookingsActivity.class));
        });

        binding.btnReportIssue.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.localconnect.ui.issue.ReportIssueActivity.class);
            startActivity(intent);
        });

        binding.btnViewNearbyIssues.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.localconnect.ui.issue.NearbyIssuesActivity.class);
            startActivity(intent);
        });

        binding.btnLogoutIcon.setOnClickListener(v -> logout());

        binding.ivUserProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, EditUserProfileActivity.class));
        });

        binding.btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.localconnect.MainActivity.class);
            intent.putExtra("from_dashboard", true);
            startActivity(intent);
        });

        loadUserData();
        loadNotices();
    }

    @javax.inject.Inject
    com.example.localconnect.data.dao.UserDao userDao;

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        String userId = prefs.getString("user_id", "");
        if (userId.isEmpty()) return;

        // Try local first
        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
            com.example.localconnect.model.User user = userDao.getUserById(userId);
            if (user != null) {
                runOnUiThread(() -> populateUserProfile(user));
            }
        });

        // Sync from Firestore
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    com.example.localconnect.model.User user = documentSnapshot.toObject(com.example.localconnect.model.User.class);
                    if (user != null) {
                        populateUserProfile(user);
                        // Update local
                        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> userDao.insert(user));
                    }
                });
    }

    private void populateUserProfile(com.example.localconnect.model.User user) {
        if (user == null) return;
        binding.tvWelcome.setText("Welcome " + user.name);
        if (user.profileImageUrl != null && !user.profileImageUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                    .load(user.profileImageUrl)
                    .placeholder(com.example.localconnect.R.drawable.ic_profile)
                    .circleCrop()
                    .into(binding.ivUserProfile);
        }
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        Intent intent = new Intent(this, com.example.localconnect.MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadNotices() {
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        String pincode = prefs.getString("user_pincode", "");

        NoticeAdapter adapter = new NoticeAdapter();
        binding.rvNotices.setAdapter(adapter);

        // Fetch from Firestore
        firestore.collection("notices")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notice> notices = queryDocumentSnapshots.toObjects(Notice.class);
                    if (!notices.isEmpty()) {
                        adapter.setNotices(notices);
                        binding.rvNotices.setVisibility(View.VISIBLE);
                        // Sync to Room
                        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                            for (Notice n : notices) noticeDao.insert(n);
                        });
                    } else {
                        // Fallback to local Room
                        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                            List<Notice> localNotices = noticeDao.getNoticesForUser(pincode);
                            runOnUiThread(() -> {
                                if (localNotices != null && !localNotices.isEmpty()) {
                                    adapter.setNotices(localNotices);
                                    binding.rvNotices.setVisibility(View.VISIBLE);
                                } else {
                                    binding.rvNotices.setVisibility(View.GONE);
                                }
                            });
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    // Firestore failed, try local
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        List<Notice> localNotices = noticeDao.getNoticesForUser(pincode);
                        runOnUiThread(() -> {
                            if (localNotices != null && !localNotices.isEmpty()) {
                                adapter.setNotices(localNotices);
                                binding.rvNotices.setVisibility(View.VISIBLE);
                            } else {
                                binding.rvNotices.setVisibility(View.GONE);
                            }
                        });
                    });
                });
    }
}
