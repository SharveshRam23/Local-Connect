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
    @javax.inject.Inject
    com.example.localconnect.data.dao.ProviderDao providerDao;

    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServiceListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> onBackPressed());

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
        firestore.collection("service_providers")
                .whereEqualTo("isApproved", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ServiceProvider> providers = queryDocumentSnapshots.toObjects(ServiceProvider.class);
                    if (!providers.isEmpty()) {
                        adapter.setProviders(providers);
                        // Sync to Room
                        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                            for (ServiceProvider p : providers) providerDao.insert(p);
                        });
                    } else {
                        // Fallback to local
                        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                            List<ServiceProvider> localProviders = providerDao.getAllApprovedProviders();
                            runOnUiThread(() -> adapter.setProviders(localProviders));
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        List<ServiceProvider> localProviders = providerDao.getAllApprovedProviders();
                        runOnUiThread(() -> adapter.setProviders(localProviders));
                    });
                });
    }

    private void filterProviders() {
        String category = binding.spinnerCategory.getSelectedItem().toString();
        String pincode = binding.etFilterPincode.getText().toString().trim();

        com.google.firebase.firestore.Query query = firestore.collection("service_providers")
                .whereEqualTo("isApproved", true);

        if (!category.equals("All")) {
            query = query.whereEqualTo("category", category);
        }
        if (!pincode.isEmpty()) {
            query = query.whereEqualTo("pincode", pincode);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ServiceProvider> providers = queryDocumentSnapshots.toObjects(ServiceProvider.class);
                    adapter.setProviders(providers);
                    if (providers.isEmpty()) {
                        Toast.makeText(this, "No matching providers found in cloud", Toast.LENGTH_SHORT).show();
                    }
                    // Sync results to room for offline fallback
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        for (ServiceProvider p : providers) providerDao.insert(p);
                    });
                })
                .addOnFailureListener(e -> {
                    // Fallback to local Room filtering
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        List<ServiceProvider> localProviders;
                        if (category.equals("All")) {
                            localProviders = pincode.isEmpty() ? providerDao.getAllApprovedProviders() : providerDao.getProvidersByPincode(pincode);
                        } else {
                            localProviders = pincode.isEmpty() ? providerDao.getProvidersByCategory(category) : providerDao.getProvidersByCategoryAndPincode(category, pincode);
                        }
                        runOnUiThread(() -> adapter.setProviders(localProviders));
                    });
                });
    }
}
