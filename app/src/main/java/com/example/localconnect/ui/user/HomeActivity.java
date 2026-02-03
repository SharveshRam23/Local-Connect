package com.example.localconnect.ui.user;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.databinding.ActivityHomeBinding;
import com.example.localconnect.viewmodel.NoticeViewModel;
import com.example.localconnect.viewmodel.ServiceProviderViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private NoticeAdapter noticeAdapter;
    private ServiceProviderAdapter serviceProviderAdapter;
    private NoticeViewModel noticeViewModel;
    private ServiceProviderViewModel serviceProviderViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.recyclerViewNotices.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewServiceProviders.setLayoutManager(new LinearLayoutManager(this));

        noticeViewModel = new ViewModelProvider(this).get(NoticeViewModel.class);
        serviceProviderViewModel = new ViewModelProvider(this).get(ServiceProviderViewModel.class);

        // Hardcoded pincode for now
        String pincode = "123456";

        noticeViewModel.getNoticesByArea(pincode).observe(this, notices -> {
            noticeAdapter = new NoticeAdapter(notices);
            binding.recyclerViewNotices.setAdapter(noticeAdapter);
        });

        serviceProviderViewModel.getApprovedProviders().observe(this, serviceProviders -> {
            serviceProviderAdapter = new ServiceProviderAdapter(serviceProviders);
            binding.recyclerViewServiceProviders.setAdapter(serviceProviderAdapter);
        });
    }
}