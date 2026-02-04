package com.example.localconnect.ui.provider;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.Notice;
import com.example.localconnect.ui.adapter.NoticeAdapter;

import java.util.List;

public class ProviderDashboardActivity extends AppCompatActivity {

    private TextView tvApprovalStatus;
    private Switch switchAvailability;
    private RecyclerView rvNotices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_dashboard);

        tvApprovalStatus = findViewById(R.id.tvApprovalStatus);
        switchAvailability = findViewById(R.id.switchAvailability);
        rvNotices = findViewById(R.id.rvProviderNotices);

        rvNotices.setLayoutManager(new LinearLayoutManager(this));

        // TODO: Load provider status from DB and update UI
        // For now, assuming static status

        switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Update availability in DB
        });

        findViewById(R.id.btnProviderLogout).setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
            prefs.edit().clear().apply();
            android.content.Intent intent = new android.content.Intent(this, com.example.localconnect.MainActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnHome).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, com.example.localconnect.MainActivity.class);
            intent.putExtra("from_dashboard", true);
            startActivity(intent);
        });

        loadNotices();
    }

    private void loadNotices() {
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        String pincode = prefs.getString("provider_pincode", "");

        NoticeAdapter adapter = new NoticeAdapter();
        rvNotices.setAdapter(adapter);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Notice> notices = AppDatabase.getDatabase(getApplicationContext()).noticeDao()
                    .getNoticesForUser(pincode);
            runOnUiThread(() -> {
                if (notices != null && !notices.isEmpty()) {
                    adapter.setNotices(notices);
                    rvNotices.setVisibility(View.VISIBLE);
                } else {
                    rvNotices.setVisibility(View.GONE);
                }
            });
        });
    }
}
