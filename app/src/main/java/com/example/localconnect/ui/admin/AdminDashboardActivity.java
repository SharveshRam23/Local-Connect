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
        Button btnViewUsers = findViewById(R.id.btnViewUsers);
        rvNotices = findViewById(R.id.rvAdminNotices);

        rvNotices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoticeAdapter();
        rvNotices.setAdapter(adapter);

        btnApproveProviders.setOnClickListener(v -> showPendingProvidersDialog());
        btnPostNotice.setOnClickListener(v -> showPostNoticeDialog());
        btnViewUsers.setOnClickListener(v -> showUsersDialog());

        loadNotices();
    }

    private void showPendingProvidersDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pending Providers");

        RecyclerView rvProviders = new RecyclerView(this);
        rvProviders.setLayoutManager(new LinearLayoutManager(this));
        com.example.localconnect.ui.adapter.ProviderAdapter providerAdapter = new com.example.localconnect.ui.adapter.ProviderAdapter();
        rvProviders.setAdapter(providerAdapter);

        builder.setView(rvProviders);
        builder.setPositiveButton("Close", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        loadPendingProviders(providerAdapter);
    }

    private void loadPendingProviders(com.example.localconnect.ui.adapter.ProviderAdapter adapter) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<com.example.localconnect.model.ServiceProvider> pendingProviders = AppDatabase
                    .getDatabase(getApplicationContext())
                    .providerDao().getPendingProviders();

            runOnUiThread(() -> {
                if (pendingProviders == null || pendingProviders.isEmpty()) {
                    Toast.makeText(this, "No pending providers.", Toast.LENGTH_SHORT).show();
                }
                adapter.setProviders(pendingProviders,
                        new com.example.localconnect.ui.adapter.ProviderAdapter.OnProviderActionListener() {
                            @Override
                            public void onApprove(com.example.localconnect.model.ServiceProvider provider) {
                                approveProvider(provider, adapter);
                            }

                            @Override
                            public void onReject(com.example.localconnect.model.ServiceProvider provider) {
                                rejectProvider(provider, adapter);
                            }
                        });
            });
        });
    }

    private void approveProvider(com.example.localconnect.model.ServiceProvider provider,
            com.example.localconnect.ui.adapter.ProviderAdapter adapter) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getDatabase(getApplicationContext()).providerDao().updateApprovalStatus(provider.id, true,
                    System.currentTimeMillis());
            runOnUiThread(() -> {
                Toast.makeText(this, "Provider Approved!", Toast.LENGTH_SHORT).show();
                // Notify Provider (Simulated or Local Notification if on same device)
                com.example.localconnect.util.NotificationUtil.showApprovalNotification(this, "Provider Approved",
                        "Approved " + provider.name);

                // Reload list
                loadPendingProviders(adapter);
            });
        });
    }

    private void rejectProvider(com.example.localconnect.model.ServiceProvider provider,
            com.example.localconnect.ui.adapter.ProviderAdapter adapter) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getDatabase(getApplicationContext()).providerDao().delete(provider);
            runOnUiThread(() -> {
                Toast.makeText(this, "Provider Rejected (Deleted)", Toast.LENGTH_SHORT).show();
                loadPendingProviders(adapter);
            });
        });

    }

    private void showUsersDialog() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<com.example.localconnect.model.User> users = AppDatabase.getDatabase(getApplicationContext()).userDao()
                    .getAllUsers();
            runOnUiThread(() -> {
                if (users == null || users.isEmpty()) {
                    Toast.makeText(this, "No users registered yet.", Toast.LENGTH_SHORT).show();
                    return;
                }

                StringBuilder sb = new StringBuilder();
                for (com.example.localconnect.model.User user : users) {
                    sb.append("Name: ").append(user.name)
                            .append("\nPhone: ").append(user.phone)
                            .append("\nPincode: ").append(user.pincode)
                            .append("\n\n");
                }

                new AlertDialog.Builder(this)
                        .setTitle("Registered Users")
                        .setMessage(sb.toString())
                        .setPositiveButton("Close", null)
                        .show();
            });
        });
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
