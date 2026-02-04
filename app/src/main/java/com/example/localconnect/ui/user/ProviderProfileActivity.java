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
    private String providerId;
    private ServiceProvider currentProvider;

    @javax.inject.Inject
    com.example.localconnect.data.dao.ProviderDao providerDao;

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
    }

    private void loadProviderDetails() {
        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
            currentProvider = providerDao.getProviderById(providerId);
            runOnUiThread(() -> {
                if (currentProvider != null) {
                    binding.tvName.setText(currentProvider.name);
                    binding.tvCategory.setText("Category: " + currentProvider.category);
                    binding.tvExperience.setText("Experience: " + currentProvider.experience + " Years");
                    binding.tvPhone.setText("Phone: " + currentProvider.phone);
                    
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
                } else {
                    Toast.makeText(this, "Provider data missing", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
