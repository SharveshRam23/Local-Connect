package com.example.localconnect.ui.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.data.dao.MandatoryServiceDao;
import com.example.localconnect.databinding.ActivityEssentialServicesBinding;
import com.example.localconnect.model.MandatoryService;
import com.example.localconnect.ui.adapter.MandatoryServiceAdapter;
import com.example.localconnect.util.LocationHelper;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EssentialServicesActivity extends AppCompatActivity {

    private ActivityEssentialServicesBinding binding;
    private MandatoryServiceAdapter adapter;
    private String currentPincode;
    private List<MandatoryService> allLoadedServices = new ArrayList<>();

    @javax.inject.Inject
    MandatoryServiceDao serviceDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEssentialServicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Get Pincode from Prefs first
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        currentPincode = prefs.getString("user_pincode", "");
        
        binding.tvLocationInfo.setText("Showing services for Pincode: " + currentPincode);

        setupRecyclerView();
        setupFilters();
        loadServices();
    }

    private void setupRecyclerView() {
        adapter = new MandatoryServiceAdapter();
        adapter.setIsAdmin(false);
        adapter.setOnServiceClickListener(new MandatoryServiceAdapter.OnServiceClickListener() {
            @Override
            public void onServiceClick(MandatoryService service) {
                Intent intent = new Intent(EssentialServicesActivity.this, MandatoryServiceDetailActivity.class);
                intent.putExtra("service_name", service.name);
                intent.putExtra("service_category", service.category);
                intent.putExtra("service_address", service.address);
                intent.putExtra("service_phone", service.phonePrimary);
                intent.putExtra("service_hours", service.workingHours);
                intent.putExtra("service_image", service.imageUrl);
                intent.putExtra("service_is24x7", service.is24x7);
                intent.putExtra("service_emergency", service.isEmergency);
                intent.putExtra("service_lat", service.latitude);
                intent.putExtra("service_lng", service.longitude);
                startActivity(intent);
            }

            @Override
            public void onEditClick(MandatoryService service) { /* No op */ }

            @Override
            public void onDeleteClick(MandatoryService service) { /* No op */ }
        });

        binding.rvServices.setLayoutManager(new LinearLayoutManager(this));
        binding.rvServices.setAdapter(adapter);
    }

    private void setupFilters() {
        binding.chipGroupCategories.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                filterList("All");
                return;
            }
            Chip chip = group.findViewById(checkedId);
            if (chip != null) {
                filterList(chip.getText().toString());
            }
        });
    }

    private void loadServices() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        // Load from Room (offline first/always for this task as data is synced via Admin)
        // Ideally we would trigger a Firestore sync here too, but for now we rely on the Admin syncing or background sync
        // For User view, we'll read Room.
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<MandatoryService> services;
            if (currentPincode != null && !currentPincode.isEmpty()) {
                services = serviceDao.getServicesByPincode(currentPincode);
            } else {
                services = serviceDao.getAllServices(); // Fallback if no pincode
            }
            
            runOnUiThread(() -> {
                binding.progressBar.setVisibility(View.GONE);
                if (services == null || services.isEmpty()) {
                    Toast.makeText(this, "No essential services found for this area.", Toast.LENGTH_SHORT).show();
                }
                allLoadedServices = services != null ? services : new ArrayList<>();
                filterList("All");
            });
        });
    }

    private void filterList(String category) {
        if (category.equalsIgnoreCase("All")) {
            adapter.setServices(allLoadedServices);
        } else {
            List<MandatoryService> filtered = new ArrayList<>();
            for (MandatoryService s : allLoadedServices) {
                if (s.category.equalsIgnoreCase(category)) {
                    filtered.add(s);
                }
            }
            adapter.setServices(filtered);
        }
    }
}
