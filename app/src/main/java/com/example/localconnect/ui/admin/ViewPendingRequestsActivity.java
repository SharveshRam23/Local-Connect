
package com.example.localconnect.ui.admin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.model.ServiceProvider;
import com.example.localconnect.viewmodel.ServiceProviderViewModel;

import java.util.ArrayList;
import java.util.List;

public class ViewPendingRequestsActivity extends AppCompatActivity {

    private ServiceProviderViewModel serviceProviderViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pending_requests);

        RecyclerView rvPendingRequests = findViewById(R.id.rvPendingRequests);
        rvPendingRequests.setLayoutManager(new LinearLayoutManager(this));

        // Dummy data for now
        List<ServiceProvider> pendingRequests = new ArrayList<>();

        PendingRequestAdapter adapter = new PendingRequestAdapter(pendingRequests);
        rvPendingRequests.setAdapter(adapter);

        serviceProviderViewModel = new ViewModelProvider(this).get(ServiceProviderViewModel.class);
        serviceProviderViewModel.getPendingProviders().observe(this, providers -> {
            adapter.setPendingRequests(providers);
        });
    }
}
