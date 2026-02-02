package com.example.localconnect.ui.user;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.Notice;
import com.example.localconnect.model.ServiceProvider;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private NoticeAdapter noticeAdapter;
    private ServiceProviderAdapter serviceProviderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        RecyclerView recyclerViewNotices = findViewById(R.id.recyclerViewNotices);
        RecyclerView recyclerViewServiceProviders = findViewById(R.id.recyclerViewServiceProviders);

        recyclerViewNotices.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewServiceProviders.setLayoutManager(new LinearLayoutManager(this));

        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Hardcoded pincode for now
            String pincode = "123456";
            List<Notice> notices = AppDatabase.getDatabase(getApplicationContext()).noticeDao().getNoticesForUser(pincode);
            List<ServiceProvider> serviceProviders = AppDatabase.getDatabase(getApplicationContext()).providerDao().getAllApprovedProviders();

            runOnUiThread(() -> {
                noticeAdapter = new NoticeAdapter(notices);
                serviceProviderAdapter = new ServiceProviderAdapter(serviceProviders);

                recyclerViewNotices.setAdapter(noticeAdapter);
                recyclerViewServiceProviders.setAdapter(serviceProviderAdapter);
            });
        });
    }
}