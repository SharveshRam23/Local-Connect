package com.example.localconnect.ui.provider;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.Booking;
import com.example.localconnect.ui.adapter.ProviderBookingAdapter;

import java.util.List;

@dagger.hilt.android.AndroidEntryPoint
public class ProviderBookingsActivity extends AppCompatActivity implements ProviderBookingAdapter.OnActionClickListener {

    private RecyclerView recyclerView;
    private ProviderBookingAdapter adapter;
    private TextView tvEmpty;
    private String providerId;

    @javax.inject.Inject
    com.example.localconnect.data.dao.BookingDao bookingDao;

    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_bookings);

        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        providerId = prefs.getString("provider_id", "");

        recyclerView = findViewById(R.id.rvProviderBookings);
        tvEmpty = findViewById(R.id.tvEmptyProviderBookings);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        tabLayout.addTab(tabLayout.newTab().setText("New"));
        tabLayout.addTab(tabLayout.newTab().setText("Upcoming"));
        tabLayout.addTab(tabLayout.newTab().setText("History"));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProviderBookingAdapter(this);
        recyclerView.setAdapter(adapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadBookings(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadBookings(0);
    }

    private void loadBookings(int tabPosition) {
        com.google.firebase.firestore.Query query = firestore.collection("bookings")
                .whereEqualTo("providerId", providerId);

        if (tabPosition == 0) {
            query = query.whereEqualTo("status", "PENDING");
        } else if (tabPosition == 1) {
            query = query.whereEqualTo("status", "ACCEPTED");
        } else {
            // Firestore doesn't support 'in' with a range and order easily without index, 
            // but we can just query all and filter or use the status list if small.
            // For simplicity, let's just query non-PENDING/non-ACCEPTED or get all and filter in app for history.
            // Actually, we can use whereIn
            query = query.whereIn("status", java.util.Arrays.asList("COMPLETED", "CANCELLED", "DECLINED"));
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Booking> bookings = queryDocumentSnapshots.toObjects(Booking.class);
                    updateUI(bookings);
                    // Sync to local
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        for (Booking b : bookings) bookingDao.insert(b);
                    });
                })
                .addOnFailureListener(e -> {
                    // Fallback to local
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        List<Booking> localBookings;
                        if (tabPosition == 0) localBookings = bookingDao.getBookingsByStatusForProvider(providerId, "PENDING");
                        else if (tabPosition == 1) localBookings = bookingDao.getBookingsByStatusForProvider(providerId, "ACCEPTED");
                        else localBookings = bookingDao.getHistoryBookingsForProvider(providerId);
                        
                        runOnUiThread(() -> updateUI(localBookings));
                    });
                });
    }

    private void updateUI(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.setBookings(bookings);
        }
    }

    @Override
    public void onApprove(Booking booking) {
        updateBookingStatus(booking, "ACCEPTED");
    }

    @Override
    public void onDecline(Booking booking) {
        updateBookingStatus(booking, "DECLINED");
    }

    @Override
    public void onComplete(Booking booking) {
        updateBookingStatus(booking, "COMPLETED");
    }

    private void updateBookingStatus(Booking booking, String status) {
        booking.status = status;
        firestore.collection("bookings").document(booking.id)
                .set(booking)
                .addOnSuccessListener(aVoid -> {
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        bookingDao.updateStatus(booking.id, status);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Booking " + status.toLowerCase() + " in cloud", Toast.LENGTH_SHORT).show();
                            
                            // Show notification
                            com.example.localconnect.util.BookingNotificationHelper.showNotification(
                                this, 
                                "Booking Updated", 
                                "Booking for " + booking.workType + " is now " + status.toLowerCase()
                            );
                            
                            TabLayout tabLayout = findViewById(R.id.tabLayout);
                            loadBookings(tabLayout.getSelectedTabPosition());
                        });
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
