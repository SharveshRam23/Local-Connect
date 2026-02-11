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
    private NoticeAdapter adapter;

    @javax.inject.Inject
    com.example.localconnect.data.dao.NoticeDao noticeDao;

    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @javax.inject.Inject
    com.example.localconnect.data.dao.MandatoryServiceDao mandatoryServiceDao;

    private com.example.localconnect.util.GeofenceHelper geofenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.rvNotices.setLayoutManager(new LinearLayoutManager(this));

        // Essential Services card click listener
        binding.btnEssentialServices.setOnClickListener(v -> {
            startActivity(new Intent(UserHomeActivity.this, EssentialServicesActivity.class));
        });

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

        // View All Notices click listener
        binding.tvViewAllNotices.setOnClickListener(v -> {
            startActivity(new Intent(this, PublicNoticeListActivity.class));
        });

        loadUserData();
        loadNotices();
        setupGeofences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    // ... (rest of methods)

    private void loadNotices() {
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        String pincode = prefs.getString("user_pincode", "");

        NoticeAdapter adapter = new NoticeAdapter();
        binding.rvNotices.setAdapter(adapter);

        // Fetch from Firestore
        firestore.collection("notices")
                .orderBy("scheduledTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notice> notices = queryDocumentSnapshots.toObjects(Notice.class);
                    if (!notices.isEmpty()) {
                        // Limit to top 3
                        List<Notice> previewList = notices.size() > 3 ? notices.subList(0, 3) : notices;
                        adapter.setNotices(previewList);
                        binding.rvNotices.setVisibility(View.VISIBLE);
                        // Sync to Room (all)
                        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                            for (Notice n : notices) noticeDao.insert(n);
                        });
                    } else {
                        // Fallback to local Room
                        loadLocalNotices(pincode, adapter);
                    }
                })
                .addOnFailureListener(e -> {
                    // Firestore failed, try local
                    loadLocalNotices(pincode, adapter);
                });
    }

    private void loadLocalNotices(String pincode, NoticeAdapter adapter) {
        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Notice> localNotices = noticeDao.getNoticesForUser(pincode);
            runOnUiThread(() -> {
                if (localNotices != null && !localNotices.isEmpty()) {
                    List<Notice> previewList = localNotices.size() > 3 ? localNotices.subList(0, 3) : localNotices;
                    adapter.setNotices(previewList);
                    binding.rvNotices.setVisibility(View.VISIBLE);
                } else {
                    binding.rvNotices.setVisibility(View.GONE);
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.releaseMediaPlayer();
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

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        String userName = prefs.getString("user_name", "User");
        binding.tvWelcome.setText("Welcome, " + userName);

        String profileImageUrl = prefs.getString("user_profile_image", "");
        if (!profileImageUrl.isEmpty()) {
            if (profileImageUrl.startsWith("data:image")) {
                String cleanBase64 = profileImageUrl;
                if (cleanBase64.contains(",")) {
                    cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
                }
                try {
                    byte[] decodedBytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT);
                    com.bumptech.glide.Glide.with(this)
                            .asBitmap()
                            .load(decodedBytes)
                            .placeholder(com.example.localconnect.R.drawable.ic_profile)
                            .into(binding.ivUserProfile);
                } catch (Exception e) {
                    binding.ivUserProfile.setImageResource(com.example.localconnect.R.drawable.ic_profile);
                }
            } else {
                com.bumptech.glide.Glide.with(this)
                        .load(profileImageUrl)
                        .placeholder(com.example.localconnect.R.drawable.ic_profile)
                        .into(binding.ivUserProfile);
            }
        }
        
        // Also fetch geofence data
        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
             List<com.example.localconnect.model.MandatoryService> services = mandatoryServiceDao.getAllServices();
             runOnUiThread(() -> {
                 if (geofenceHelper != null) {
                     geofenceHelper.addGeofencesForServices(services);
                 }
             });
        });
    }

    private void setupGeofences() {
        geofenceHelper = new com.example.localconnect.util.GeofenceHelper(this);
    }
}
