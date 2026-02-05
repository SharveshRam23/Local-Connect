package com.example.localconnect.ui.user;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.Rating;
import com.example.localconnect.model.ServiceProvider;

@dagger.hilt.android.AndroidEntryPoint
public class RateProviderActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private TextView tvRatingValue, tvProviderName;
    private EditText etReview;
    private Button btnSubmitRating, btnCancel;
    
    private String providerId;
    private String bookingId;
    private String providerName;

    @javax.inject.Inject
    com.example.localconnect.data.dao.RatingDao ratingDao;

    @javax.inject.Inject
    com.example.localconnect.data.dao.ProviderDao providerDao;

    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_provider);

        // Get provider info from intent
        providerId = getIntent().getStringExtra("provider_id");
        bookingId = getIntent().getStringExtra("booking_id");
        providerName = getIntent().getStringExtra("provider_name");

        if (providerId == null) {
            Toast.makeText(this, "Error: Provider not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        ratingBar = findViewById(R.id.ratingBar);
        tvRatingValue = findViewById(R.id.tvRatingValue);
        tvProviderName = findViewById(R.id.tvProviderName);
        etReview = findViewById(R.id.etReview);
        btnSubmitRating = findViewById(R.id.btnSubmitRating);
        btnCancel = findViewById(R.id.btnCancel);

        if (providerName != null) {
            tvProviderName.setText(providerName);
        }
    }

    private void setupListeners() {
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            tvRatingValue.setText(String.format("%.1f / 5.0", rating));
        });

        btnSubmitRating.setOnClickListener(v -> submitRating());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void submitRating() {
        float ratingValue = ratingBar.getRating();
        
        if (ratingValue == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        String reviewText = etReview.getText().toString().trim();
        
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        String userId = prefs.getString("user_id", "");
        String userName = prefs.getString("user_name", "Anonymous");

        btnSubmitRating.setEnabled(false);
        btnSubmitRating.setText("Submitting...");

        // Create rating object
        Rating rating = new Rating(userId, providerId, bookingId, ratingValue, reviewText, userName);

        // Save to Firestore
        firestore.collection("ratings")
                .document(rating.id)
                .set(rating)
                .addOnSuccessListener(aVoid -> {
                    // Sync to local Room
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        ratingDao.insert(rating);
                        updateProviderRatingCloud();
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Rating submitted to cloud!", Toast.LENGTH_LONG).show();
                            finish();
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSubmitRating.setEnabled(true);
                    btnSubmitRating.setText("Submit Rating");
                });
    }

    private void updateProviderRatingCloud() {
        // Fetch all ratings for this provider from Firestore to calculate new average
        firestore.collection("ratings")
                .whereEqualTo("providerId", providerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Rating> ratings = queryDocumentSnapshots.toObjects(Rating.class);
                    if (!ratings.isEmpty()) {
                        float total = 0;
                        for (Rating r : ratings) total += r.rating;
                        float avg = total / ratings.size();
                        int count = ratings.size();

                        // Update provider in Firestore
                        firestore.collection("service_providers").document(providerId)
                                .update("rating", avg, "ratingCount", count)
                                .addOnSuccessListener(aVoid -> {
                                    // Also sync to local provider table
                                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                                        providerDao.updateRating(providerId, avg, count);
                                    });
                                });
                    }
                });
    }
}
