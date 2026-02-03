package com.example.localconnect.ui.admin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.localconnect.databinding.ActivityViewPendingRequestsBinding;
import com.example.localconnect.viewmodel.ServiceProviderViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ViewPendingRequestsActivity extends AppCompatActivity {

    private ActivityViewPendingRequestsBinding binding;
    private ServiceProviderViewModel serviceProviderViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewPendingRequestsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.rvPendingRequests.setLayoutManager(new LinearLayoutManager(this));

        PendingRequestAdapter adapter = new PendingRequestAdapter(new ArrayList<>());
        binding.rvPendingRequests.setAdapter(adapter);

        serviceProviderViewModel = new ViewModelProvider(this).get(ServiceProviderViewModel.class);
        serviceProviderViewModel.getPendingProviders().observe(this, adapter::setPendingRequests);
    }
}
