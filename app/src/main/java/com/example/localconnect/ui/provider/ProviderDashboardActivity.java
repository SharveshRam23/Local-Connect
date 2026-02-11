package com.example.localconnect.ui.provider;

import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.bumptech.glide.Glide;

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
    private SwitchMaterial switchAvailability;
    private RecyclerView rvNotices;
    private RecyclerView rvAppointments;
    private com.example.localconnect.ui.adapter.ProviderBookingAdapter appointmentAdapter;

    @javax.inject.Inject
    com.example.localconnect.data.dao.ProviderDao providerDao;

    @javax.inject.Inject
    com.example.localconnect.data.dao.NoticeDao noticeDao;

    @javax.inject.Inject
    com.example.localconnect.data.dao.BookingDao bookingDao;

    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("LocalConnect", "ProviderDashboardActivity onCreate started");
        try {
            setContentView(R.layout.activity_provider_dashboard);
            android.util.Log.d("LocalConnect", "ProviderDashboard layout inflated");

            tvApprovalStatus = findViewById(R.id.tvApprovalStatus);
            switchAvailability = findViewById(R.id.switchAvailability);
            rvNotices = findViewById(R.id.rvProviderNotices);
            rvAppointments = findViewById(R.id.rvProviderAppointments);
            
            android.util.Log.d("LocalConnect", "Views initialized");

            rvNotices.setLayoutManager(new LinearLayoutManager(this));
            rvAppointments.setLayoutManager(new LinearLayoutManager(this));
            
            appointmentAdapter = new com.example.localconnect.ui.adapter.ProviderBookingAdapter(new com.example.localconnect.ui.adapter.ProviderBookingAdapter.OnActionClickListener() {
                @Override
                public void onApprove(com.example.localconnect.model.Booking booking) {
                    updateBookingStatus(booking, "ACCEPTED");
                }

                @Override
                public void onDecline(com.example.localconnect.model.Booking booking) {
                    updateBookingStatus(booking, "DECLINED");
                }

                @Override
                public void onComplete(com.example.localconnect.model.Booking booking) {
                    updateBookingStatus(booking, "COMPLETED");
                }
            });
            rvAppointments.setAdapter(appointmentAdapter);

            switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateAvailability(isChecked);
            });

            findViewById(R.id.btnProviderLogoutIcon).setOnClickListener(v -> logout());

            findViewById(R.id.ivProviderProfile).setOnClickListener(v -> {
                startActivity(new android.content.Intent(this, EditProviderProfileActivity.class));
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
            
            findViewById(R.id.tvViewAllNotices).setOnClickListener(v -> {
                 startActivity(new android.content.Intent(this, com.example.localconnect.ui.user.PublicNoticeListActivity.class));
            });

            loadProviderData();
            loadNotices();
            loadAppointments();
            android.util.Log.d("LocalConnect", "ProviderDashboard onCreate finished successfully");
        } catch (Exception e) {
            android.util.Log.e("LocalConnect", "Error in ProviderDashboard onCreate", e);
            Toast.makeText(this, "Startup Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ... (rest of methods)

    private void loadNotices() {
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        String pincode = prefs.getString("provider_pincode", "");

        com.example.localconnect.ui.adapter.NoticeAdapter adapter = new com.example.localconnect.ui.adapter.NoticeAdapter();
        rvNotices.setAdapter(adapter);

        firestore.collection("notices")
                .orderBy("scheduledTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<com.example.localconnect.model.Notice> notices = queryDocumentSnapshots.toObjects(com.example.localconnect.model.Notice.class);
                    if (!notices.isEmpty()) {
                        List<com.example.localconnect.model.Notice> previewList = notices.size() > 3 ? notices.subList(0, 3) : notices;
                        adapter.setNotices(previewList);
                        rvNotices.setVisibility(View.VISIBLE);
                        // Sync to Room
                        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                            for (com.example.localconnect.model.Notice n : notices) noticeDao.insert(n);
                        });
                    } else {
                         loadLocalNotices(pincode, adapter);
                    }
                })
                .addOnFailureListener(e -> {
                     loadLocalNotices(pincode, adapter);
                });
    }

    private void loadLocalNotices(String pincode, com.example.localconnect.ui.adapter.NoticeAdapter adapter) {
         com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
            List<com.example.localconnect.model.Notice> localNotices = noticeDao.getNoticesForUser(pincode);
            runOnUiThread(() -> {
                if (localNotices != null && !localNotices.isEmpty()) {
                    List<com.example.localconnect.model.Notice> previewList = localNotices.size() > 3 ? localNotices.subList(0, 3) : localNotices;
                    adapter.setNotices(previewList);
                    rvNotices.setVisibility(View.VISIBLE);
                } else {
                    rvNotices.setVisibility(View.GONE);
                }
            });
        });
    }

    private void loadAppointments() {
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        String providerId = prefs.getString("provider_id", "");
        if (providerId.isEmpty()) return;

        firestore.collection("bookings")
                .whereEqualTo("providerId", providerId)
                .whereIn("status", java.util.Arrays.asList("PENDING", "ACCEPTED"))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<com.example.localconnect.model.Booking> bookings = queryDocumentSnapshots.toObjects(com.example.localconnect.model.Booking.class);
                    appointmentAdapter.setBookings(bookings);
                    rvAppointments.setVisibility(bookings.isEmpty() ? View.GONE : View.VISIBLE);
                    
                    // Sync to Room
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        for (com.example.localconnect.model.Booking b : bookings) bookingDao.insert(b);
                    });
                })
                .addOnFailureListener(e -> {
                    // Fallback to local
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        List<com.example.localconnect.model.Booking> localBookings = bookingDao.getBookingsByStatusForProvider(providerId, "PENDING");
                        localBookings.addAll(bookingDao.getBookingsByStatusForProvider(providerId, "ACCEPTED"));
                        runOnUiThread(() -> {
                            appointmentAdapter.setBookings(localBookings);
                            rvAppointments.setVisibility(localBookings.isEmpty() ? View.GONE : View.VISIBLE);
                        });
                    });
                });
    }

    private void updateBookingStatus(com.example.localconnect.model.Booking booking, String status) {
        booking.status = status;
        firestore.collection("bookings").document(booking.id)
                .set(booking)
                .addOnSuccessListener(aVoid -> {
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        bookingDao.updateStatus(booking.id, status);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Booking " + status.toLowerCase(), Toast.LENGTH_SHORT).show();
                            loadAppointments(); // Refresh
                        });
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        android.content.Intent intent = new android.content.Intent(this, com.example.localconnect.MainActivity.class);
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void loadProviderData() {
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        String providerId = prefs.getString("provider_id", "");
        if (providerId.isEmpty()) return;

        firestore.collection("service_providers").document(providerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    com.example.localconnect.model.ServiceProvider provider = documentSnapshot.toObject(com.example.localconnect.model.ServiceProvider.class);
                    if (provider != null) {
                        // Update UI
                        if (provider.isApproved) {
                            tvApprovalStatus.setText("Status: Approved");
                            tvApprovalStatus.setTextColor(android.graphics.Color.GREEN);
                            switchAvailability.setEnabled(true);
                            switchAvailability.setChecked(provider.isAvailable);
                            
                            TextView tvWelcomeName = findViewById(R.id.tvWelcomeName);
                            if (tvWelcomeName != null) {
                                tvWelcomeName.setText("Welcome, " + provider.name);
                            }

                            // Update Rating
                            RatingBar ratingBar = findViewById(R.id.ratingBar);
                            TextView tvRating = findViewById(R.id.tvRating);
                            if (ratingBar != null) {
                                ratingBar.setRating(provider.rating);
                            }
                            if (tvRating != null) {
                                tvRating.setText(String.format("%.1f (%d)", provider.rating, provider.ratingCount));
                            }

                            // Update Profile Image
                            ImageView ivProfile = findViewById(R.id.ivProviderProfile);
                            if (ivProfile != null && provider.profileImageUrl != null && !provider.profileImageUrl.isEmpty()) {
                                if (provider.profileImageUrl.startsWith("data:image")) {
                                    byte[] decodedString = android.util.Base64.decode(provider.profileImageUrl.substring(provider.profileImageUrl.indexOf(",") + 1), android.util.Base64.DEFAULT);
                                    android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    ivProfile.setImageBitmap(decodedByte);
                                } else {
                                    Glide.with(this).load(provider.profileImageUrl).placeholder(R.drawable.ic_profile).into(ivProfile);
                                }
                            }
                        } else {
                            tvApprovalStatus.setText("Status: Pending Approval");
                            tvApprovalStatus.setTextColor(android.graphics.Color.RED);
                            switchAvailability.setEnabled(false);
                        }
                    }
                });
    }

    private void updateAvailability(boolean isAvailable) {
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        String providerId = prefs.getString("provider_id", "");
        if (providerId.isEmpty()) return;

        firestore.collection("service_providers").document(providerId)
                .update("isAvailable", isAvailable)
                .addOnSuccessListener(aVoid -> {
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                         // Need updateAvailability method in ProviderDao or just update the object
                         // For now, assume it's synced. Or simpler:
                    });
                    Toast.makeText(this, "Availability updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update availability", Toast.LENGTH_SHORT).show();
                    // Revert switch if failed
                    switchAvailability.setChecked(!isAvailable);
                });
    }
}
