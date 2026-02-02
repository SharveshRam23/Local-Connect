package com.example.localconnect.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.Notice;
import com.example.localconnect.ui.adapter.NoticeAdapter;

import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button btnApproveProviders, btnPostNotice;
    private RecyclerView rvNotices;
    private NoticeAdapter adapter;
    private static final int PERMISSION_REQUEST_SMS = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        btnApproveProviders = findViewById(R.id.btnApproveProviders);
        btnPostNotice = findViewById(R.id.btnPostNotice);
        rvNotices = findViewById(R.id.rvAdminNotices);

        rvNotices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoticeAdapter();
        rvNotices.setAdapter(adapter);

        btnApproveProviders.setOnClickListener(v -> {
            // TODO: Implement Approval Logic (Dialog or new Activity)
            Toast.makeText(this, "Feature coming soon: Provider Approval", Toast.LENGTH_SHORT).show();
        });

        btnPostNotice.setOnClickListener(v -> showPostNoticeDialog());

        loadNotices();
    }

    private void showPostNoticeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Post Announcement");

        final EditText input = new EditText(this);
        input.setHint("Enter announcement here...");
        input.setMaxLines(5);
        builder.setView(input);

        builder.setPositiveButton("Post", (dialog, which) -> {
            String content = input.getText().toString().trim();
            if (!content.isEmpty()) {
                if (checkSmsPermission()) {
                    postNotice(content);
                } else {
                    requestSmsPermission();
                    postNotice(content);
                }
            } else {
                Toast.makeText(this, "Announcement cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private boolean checkSmsPermission() {
        return androidx.core.content.ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.SEND_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private void requestSmsPermission() {
        androidx.core.app.ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.SEND_SMS },
                PERMISSION_REQUEST_SMS);
    }

    private void postNotice(String content) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Notice notice = new Notice("Announcement", content, "GLOBAL", null,
                    System.currentTimeMillis());
            AppDatabase.getDatabase(getApplicationContext()).noticeDao().insert(notice);

            // Broadcast SMS
            if (checkSmsPermission()) {
                try {
                    List<String> phoneNumbers = AppDatabase.getDatabase(getApplicationContext()).userDao()
                            .getAllUserPhones();
                    android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
                    for (String phone : phoneNumbers) {
                        if (phone != null && !phone.isEmpty()) {
                            try {
                                smsManager.sendTextMessage(phone, null, "LocalConnect: " + content, null, null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Announcement Posted & SMS Sent", Toast.LENGTH_SHORT).show();
                loadNotices();
            });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions,
            @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_SMS) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadNotices() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Notice> notices = AppDatabase.getDatabase(getApplicationContext()).noticeDao().getAllNotices();
            runOnUiThread(() -> {
                if (notices != null) {
                    adapter.setNotices(notices);
                }
            });
        });
    }
}
