package com.example.localconnect.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.localconnect.databinding.ActivityProviderProfileBinding;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.ServiceProvider;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProviderProfileActivity extends AppCompatActivity {

    private ActivityProviderProfileBinding binding;
    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @javax.inject.Inject
    com.example.localconnect.data.dao.ProviderDao providerDao;

    private String providerId;
    private ServiceProvider currentProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProviderProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        providerId = getIntent().getStringExtra("provider_id");
        if (providerId == null) {
            Toast.makeText(this, "Error: Provider not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProviderDetails();

        binding.btnCall.setOnClickListener(v -> {
            if (currentProvider != null) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(android.net.Uri.parse("tel:" + currentProvider.phone));
                startActivity(intent);
            }
        });

        binding.btnSms.setOnClickListener(v -> {
            if (currentProvider != null) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(android.net.Uri.parse("smsto:" + currentProvider.phone));
                startActivity(intent);
            }
        });

        binding.btnBookNow.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra("provider_id", providerId);
            startActivity(intent);
        });

        binding.btnRateProvider.setOnClickListener(v -> {
            if (currentProvider != null) {
                Intent intent = new Intent(this, RateProviderActivity.class);
                intent.putExtra("provider_id", providerId);
                intent.putExtra("provider_name", currentProvider.name);
                startActivity(intent);
            }
        });

        binding.btnViewAllReviews.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProviderRatingsActivity.class);
            intent.putExtra("provider_id", providerId);
            intent.putExtra("provider_name", currentProvider.name != null ? currentProvider.name : "Provider");
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload provider details to show updated ratings
        loadProviderDetails();
    }

    private void loadProviderDetails() {
        // First check Room for immediate display
        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
            currentProvider = providerDao.getProviderById(providerId);
            if (currentProvider != null) {
                runOnUiThread(() -> updateUI());
            }
            
            // Sync from Firestore for latest data
            firestore.collection("service_providers").document(providerId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        ServiceProvider provider = documentSnapshot.toObject(ServiceProvider.class);
                        if (provider != null) {
                            currentProvider = provider;
                            updateUI();
                            // Update Room
                            com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> providerDao.insert(provider));
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (currentProvider == null) {
                            Toast.makeText(this, "Error loading cloud data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void updateUI() {
        if (currentProvider == null) return;
        
        binding.tvName.setText(currentProvider.name);
        binding.chipCategory.setText(currentProvider.category);
        binding.tvExperience.setText("Experience: " + currentProvider.experience + " Years");
        binding.tvPhone.setText("Phone: " + currentProvider.phone);
        
        // Display rating
        if (currentProvider.ratingCount > 0) {
            binding.ratingBar.setRating(currentProvider.rating);
            binding.tvRating.setText(String.format("%.1f (%d reviews)", currentProvider.rating, currentProvider.ratingCount));
            binding.ratingBar.setVisibility(android.view.View.VISIBLE);
            binding.tvRating.setVisibility(android.view.View.VISIBLE);
            binding.btnViewAllReviews.setVisibility(android.view.View.VISIBLE);
        } else {
            binding.tvRating.setText("No ratings yet");
            binding.ratingBar.setVisibility(android.view.View.GONE);
            binding.tvRating.setVisibility(android.view.View.VISIBLE);
            binding.btnViewAllReviews.setVisibility(android.view.View.GONE);
        }
        
        if (currentProvider.isAvailable) {
            binding.tvAvailability.setText("● Available");
            binding.tvAvailability.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            binding.btnBookNow.setEnabled(true);
        } else {
            binding.tvAvailability.setText("● Not Available");
            binding.tvAvailability.setTextColor(android.graphics.Color.RED);
            binding.btnBookNow.setEnabled(false);
            binding.btnBookNow.setText("Currently Unavailable");
        }
    }
}
