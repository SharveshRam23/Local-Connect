package com.example.localconnect.ui.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.Booking;
import com.example.localconnect.ui.adapter.BookingAdapter;

import java.util.List;

@dagger.hilt.android.AndroidEntryPoint
public class MyBookingsActivity extends AppCompatActivity implements BookingAdapter.OnBookingActionListener {

    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private TextView tvEmpty;
    private String userId;

    @javax.inject.Inject
    com.example.localconnect.data.dao.BookingDao bookingDao;

    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        userId = prefs.getString("user_id", "");

        recyclerView = findViewById(R.id.rvMyBookings);
        tvEmpty = findViewById(R.id.tvEmptyBookings);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(this);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadBookings();
    }

    private void loadBookings() {
        firestore.collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Booking> bookings = queryDocumentSnapshots.toObjects(Booking.class);
                    if (!bookings.isEmpty()) {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.setBookings(bookings);
                        // Sync to local
                        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                            for (Booking b : bookings) bookingDao.insert(b);
                        });
                    } else {
                        // Fallback to local
                        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                            List<Booking> localBookings = bookingDao.getBookingsForUser(userId);
                            runOnUiThread(() -> {
                                if (localBookings == null || localBookings.isEmpty()) {
                                    tvEmpty.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.GONE);
                                } else {
                                    tvEmpty.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                    adapter.setBookings(localBookings);
                                }
                            });
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        List<Booking> localBookings = bookingDao.getBookingsForUser(userId);
                        runOnUiThread(() -> {
                            if (localBookings != null) {
                                adapter.setBookings(localBookings);
                            } else {
                                Toast.makeText(this, "Cloud Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                });
    }

    @Override
    public void onCancel(Booking booking) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    booking.status = "CANCELLED";
                    firestore.collection("bookings").document(booking.id)
                            .set(booking)
                            .addOnSuccessListener(aVoid -> {
                                com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                                    bookingDao.updateStatus(booking.id, "CANCELLED");
                                    runOnUiThread(() -> {
                                        Toast.makeText(this, "Booking cancelled in cloud", Toast.LENGTH_SHORT).show();
                                        loadBookings();
                                    });
                                });
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Cancel failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onRate(Booking booking) {
        Intent intent = new Intent(this, RateProviderActivity.class);
        intent.putExtra("provider_id", booking.providerId);
        intent.putExtra("booking_id", booking.id);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookings();
    }
}
