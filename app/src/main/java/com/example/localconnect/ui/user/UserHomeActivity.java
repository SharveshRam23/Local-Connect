package com.example.localconnect.ui.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.Notice;
import com.example.localconnect.ui.issue.ReportIssueActivity;
import com.example.localconnect.ui.adapter.NoticeAdapter;

import java.util.ArrayList;
import java.util.List;

public class UserHomeActivity extends AppCompatActivity {

    private RecyclerView rvNotices;
    private Button btnFindServices, btnReportIssue;
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        rvNotices = findViewById(R.id.rvNotices);
        btnFindServices = findViewById(R.id.btnFindServices);
        btnReportIssue = findViewById(R.id.btnReportIssue);
        tvWelcome = findViewById(R.id.tvWelcome);

        rvNotices.setLayoutManager(new LinearLayoutManager(this));

        // Basic navigation setup
        btnFindServices.setOnClickListener(v -> {
            startActivity(new Intent(UserHomeActivity.this, ServiceListActivity.class));
        });

        btnReportIssue.setOnClickListener(v -> {
            // Assuming ReportIssueActivity will be created
            Intent intent = new Intent(this, com.example.localconnect.ui.issue.ReportIssueActivity.class);
            startActivity(intent);
        });

        loadNotices();
    }

    private void loadNotices() {
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        String pincode = prefs.getString("user_pincode", "");

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
                    // Optional: Show "No notices" text
                    rvNotices.setVisibility(View.GONE);
                }
            });
        });
    }
}
