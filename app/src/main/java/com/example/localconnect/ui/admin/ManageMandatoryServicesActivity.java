package com.example.localconnect.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.localconnect.databinding.ActivityManageMandatoryServicesBinding;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.data.dao.MandatoryServiceDao;
import com.example.localconnect.model.MandatoryService;
import com.example.localconnect.ui.adapter.MandatoryServiceAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManageMandatoryServicesActivity extends AppCompatActivity {

    private ActivityManageMandatoryServicesBinding binding;
    private MandatoryServiceAdapter adapter;
    private ListenerRegistration firestoreListener;

    @javax.inject.Inject
    MandatoryServiceDao serviceDao;

    @javax.inject.Inject
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageMandatoryServicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupRecyclerView();

        binding.fabAddService.setOnClickListener(v -> {
            startActivity(new Intent(this, AddMandatoryServiceActivity.class));
        });

        loadServices();
    }

    private void setupRecyclerView() {
        adapter = new MandatoryServiceAdapter();
        adapter.setIsAdmin(true);
        adapter.setOnServiceClickListener(new MandatoryServiceAdapter.OnServiceClickListener() {
            @Override
            public void onServiceClick(MandatoryService service) {
                // Could show details
            }

            @Override
            public void onEditClick(MandatoryService service) {
                Intent intent = new Intent(ManageMandatoryServicesActivity.this, AddMandatoryServiceActivity.class);
                intent.putExtra("service_id", service.id);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(MandatoryService service) {
                new AlertDialog.Builder(ManageMandatoryServicesActivity.this)
                        .setTitle("Delete Service")
                        .setMessage("Are you sure you want to delete " + service.name + "?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteService(service))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        binding.rvMandatoryServices.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMandatoryServices.setAdapter(adapter);
    }

    private void loadServices() {
        // Real-time listener from Firestore
        firestoreListener = firestore.collection("mandatory_services")
                .orderBy("lastUpdated", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        loadFromRoom();
                        return;
                    }

                    if (value != null) {
                        List<MandatoryService> services = value.toObjects(MandatoryService.class);
                        adapter.setServices(services);
                        syncToRoom(services);
                    }
                });
    }

    private void loadFromRoom() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<MandatoryService> localServices = serviceDao.getAllServices();
            runOnUiThread(() -> adapter.setServices(localServices));
        });
    }

    private void syncToRoom(List<MandatoryService> services) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            for (MandatoryService service : services) {
                serviceDao.insert(service);
            }
        });
    }

    private void deleteService(MandatoryService service) {
        firestore.collection("mandatory_services").document(service.id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        serviceDao.delete(service);
                        runOnUiThread(() -> Toast.makeText(this, "Service deleted", Toast.LENGTH_SHORT).show());
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
    }
}
