package com.example.localconnect.ui.user;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.localconnect.databinding.ActivityBookingBinding;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.Booking;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BookingActivity extends AppCompatActivity {

    private ActivityBookingBinding binding;
    private String providerId;
    private String userId;
    private com.example.localconnect.model.ServiceProvider currentProvider;

    @javax.inject.Inject
    com.example.localconnect.data.dao.BookingDao bookingDao;
    @javax.inject.Inject
    com.example.localconnect.data.dao.ProviderDao providerDao;
    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        providerId = getIntent().getStringExtra("provider_id");
        
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        userId = prefs.getString("user_id", null);

        if (providerId == null || userId == null) {
            Toast.makeText(this, "Error: Session or Provider not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProvider();

        binding.btnSubmitBooking.setOnClickListener(v -> submitBooking());
    }

    private void loadProvider() {
        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
            currentProvider = providerDao.getProviderById(providerId);
            if (currentProvider == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Provider not found", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void submitBooking() {
        String workType = binding.etWorkType.getText().toString().trim();
        String date = binding.etDate.getText().toString().trim(); // Format: YYYY-MM-DD
        String time = binding.etTime.getText().toString().trim(); // Format: HH:MM
        String details = binding.etDetails.getText().toString().trim(); // Using etDetails validation if exists

        if (TextUtils.isEmpty(workType) || TextUtils.isEmpty(date) || TextUtils.isEmpty(time)) {
            Toast.makeText(this, "Work, Date and Time are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Basic Time Validation
        if (currentProvider != null) {
            String[] timeParts = time.split(":");
            if (timeParts.length == 2) {
                String from = currentProvider.availableFrom != null ? currentProvider.availableFrom : "09:00";
                String to = currentProvider.availableTo != null ? currentProvider.availableTo : "18:00";
                
                if (time.compareTo(from) < 0 || time.compareTo(to) > 0) {
                     Toast.makeText(this, "Provider is available between " + from + " and " + to, Toast.LENGTH_LONG).show();
                     return;
                }
            }
        }

        Booking booking = new Booking(userId, providerId, workType, date + " " + time, details);
        
        // Save to Firestore
        firestore.collection("bookings")
                .document(booking.id)
                .set(booking)
                .addOnSuccessListener(aVoid -> {
                    // Sync to local Room
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        bookingDao.insert(booking);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Booking Requested and synced to cloud!", Toast.LENGTH_LONG).show();
                            
                            // Notify provider (simulated local)
                            com.example.localconnect.util.BookingNotificationHelper.showNotification(
                                this, 
                                "New Booking Request", 
                                "You have a new request for " + workType
                            );
                            
                            finish();
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
