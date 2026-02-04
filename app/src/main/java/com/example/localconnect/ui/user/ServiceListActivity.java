package com.example.localconnect.ui.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.localconnect.databinding.ActivityServiceListBinding;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.ServiceProvider;
import com.example.localconnect.ui.adapter.ProviderAdapter;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ServiceListActivity extends AppCompatActivity {

    private ActivityServiceListBinding binding;
    private ProviderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServiceListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.rvProviders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProviderAdapter(provider -> {
            Intent intent = new Intent(this, ProviderProfileActivity.class);
            intent.putExtra("provider_id", provider.id);
            startActivity(intent);
        });
        binding.rvProviders.setAdapter(adapter);

        setupSpinner();

        binding.btnFilter.setOnClickListener(v -> filterProviders());

        // Default: Load nearby providers
        loadNearbyProviders();
    }

    private void setupSpinner() {
        String[] categories = { "All", "Plumber", "Electrician", "Carpenter", "Maid" };
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(spinnerAdapter);
    }

    private void loadNearbyProviders() {
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        String pincode = prefs.getString("user_pincode", "");
        if (!pincode.isEmpty()) {
            binding.etFilterPincode.setText(pincode);
            filterProviders();
        } else {
            loadAllProviders();
        }
    }

    private void loadAllProviders() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<ServiceProvider> providers = AppDatabase.getDatabase(getApplicationContext())
                    .providerDao().getAllApprovedProviders();
            runOnUiThread(() -> adapter.setProviders(providers));
        });
    }

    private void filterProviders() {
        String category = binding.spinnerCategory.getSelectedItem().toString();
        String pincode = binding.etFilterPincode.getText().toString().trim();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<ServiceProvider> providers;
            if (category.equals("All")) {
                if (pincode.isEmpty()) {
                    providers = AppDatabase.getDatabase(getApplicationContext()).providerDao().getAllApprovedProviders();
                } else {
                    providers = AppDatabase.getDatabase(getApplicationContext()).providerDao().getProvidersByPincode(pincode);
                }
            } else {
                if (pincode.isEmpty()) {
                    providers = AppDatabase.getDatabase(getApplicationContext()).providerDao().getProvidersByCategory(category);
                } else {
                    providers = AppDatabase.getDatabase(getApplicationContext()).providerDao().getProvidersByCategoryAndPincode(category, pincode);
                }
            }
            runOnUiThread(() -> {
                adapter.setProviders(providers);
                if (providers.isEmpty()) {
                    Toast.makeText(this, "No providers found", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
