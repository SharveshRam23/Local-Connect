package com.example.localconnect.ui.provider;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.Notice;
import com.example.localconnect.ui.adapter.NoticeAdapter;

import java.util.List;

@dagger.hilt.android.AndroidEntryPoint
public class ProviderDashboardActivity extends AppCompatActivity {

    private TextView tvApprovalStatus;
    private Switch switchAvailability;
    private RecyclerView rvNotices;

    @javax.inject.Inject
    com.example.localconnect.data.dao.ProviderDao providerDao;

    @javax.inject.Inject
    com.example.localconnect.data.dao.NoticeDao noticeDao;

    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_dashboard);

        tvApprovalStatus = findViewById(R.id.tvApprovalStatus);
        switchAvailability = findViewById(R.id.switchAvailability);
        rvNotices = findViewById(R.id.rvProviderNotices);

        rvNotices.setLayoutManager(new LinearLayoutManager(this));

        switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateAvailability(isChecked);
        });

        findViewById(R.id.btnProviderLogout).setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
            prefs.edit().clear().apply();
            android.content.Intent intent = new android.content.Intent(this, com.example.localconnect.MainActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnHome).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, com.example.localconnect.MainActivity.class);
            intent.putExtra("from_dashboard", true);
            startActivity(intent);
        });

        findViewById(R.id.btnManageBookings).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, ProviderBookingsActivity.class);
            startActivity(intent);
        });

        loadProviderData();
        loadNotices();
    }

    private void loadProviderData() {
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        String providerId = prefs.getString("provider_id", "");
        if (providerId.isEmpty()) return;

        // Fetch from Firestore
        firestore.collection("service_providers").document(providerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    com.example.localconnect.model.ServiceProvider provider = documentSnapshot.toObject(com.example.localconnect.model.ServiceProvider.class);
                    if (provider != null) {
                        updateUI(provider);
                        // Sync to local
                        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> providerDao.insert(provider));
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback to local
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        com.example.localconnect.model.ServiceProvider localProvider = providerDao.getProviderById(providerId);
                        runOnUiThread(() -> updateUI(localProvider));
                    });
                });
    }

    private void updateUI(com.example.localconnect.model.ServiceProvider provider) {
        if (provider == null) return;
        tvApprovalStatus.setText("Approval Status: " + (provider.isApproved ? "Approved" : "Pending"));
        switchAvailability.setChecked(provider.isAvailable);
        
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        TextView tvRating = findViewById(R.id.tvRating);
        
        if (provider.ratingCount > 0) {
            ratingBar.setRating(provider.rating);
            tvRating.setText(String.format("%.1f (%d)", provider.rating, provider.ratingCount));
        } else {
            tvRating.setText("No ratings yet");
        }
    }

    private void updateAvailability(boolean isAvailable) {
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        String providerId = prefs.getString("provider_id", "");
        if (providerId.isEmpty()) return;

        firestore.collection("service_providers").document(providerId)
                .update("isAvailable", isAvailable)
                .addOnSuccessListener(aVoid -> {
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        providerDao.updateAvailability(providerId, isAvailable);
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update availability: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadNotices() {
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        String pincode = prefs.getString("provider_pincode", "");

        NoticeAdapter adapter = new NoticeAdapter();
        rvNotices.setAdapter(adapter);

        firestore.collection("notices")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notice> notices = queryDocumentSnapshots.toObjects(Notice.class);
                    if (!notices.isEmpty()) {
                        adapter.setNotices(notices);
                        rvNotices.setVisibility(View.VISIBLE);
                        // Sync to Room
                        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                            for (Notice n : notices) noticeDao.insert(n);
                        });
                    } else {
                        // Fallback to local
                        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                            List<Notice> localNotices = noticeDao.getNoticesForUser(pincode);
                            runOnUiThread(() -> {
                                if (localNotices != null && !localNotices.isEmpty()) {
                                    adapter.setNotices(localNotices);
                                    rvNotices.setVisibility(View.VISIBLE);
                                } else {
                                    rvNotices.setVisibility(View.GONE);
                                }
                            });
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        List<Notice> localNotices = noticeDao.getNoticesForUser(pincode);
                        runOnUiThread(() -> {
                            if (localNotices != null && !localNotices.isEmpty()) {
                                adapter.setNotices(localNotices);
                                rvNotices.setVisibility(View.VISIBLE);
                            } else {
                                rvNotices.setVisibility(View.GONE);
                            }
                        });
                    });
                });
    }
}
