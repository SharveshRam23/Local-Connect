package com.example.localconnect.ui.admin;

import android.app.AlertDialog;
import android.content.Intent;
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
import com.example.localconnect.model.ServiceProvider;
import com.example.localconnect.ui.adapter.NoticeAdapter;
import com.example.localconnect.ui.adapter.ProviderAdapter;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AdminDashboardActivity extends AppCompatActivity {

    @javax.inject.Inject
    com.example.localconnect.data.dao.UserDao userDao;

    @javax.inject.Inject
    com.example.localconnect.data.dao.ProviderDao providerDao;

    @javax.inject.Inject
    com.example.localconnect.data.dao.NoticeDao noticeDao;

    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

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
        findViewById(R.id.btnManageIssues).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminIssueListActivity.class));
        });

        findViewById(R.id.btnAdminLogout).setOnClickListener(v -> {
            android.content.SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
            prefs.edit().clear().apply();
            Intent intent = new Intent(this, com.example.localconnect.MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.localconnect.MainActivity.class);
            intent.putExtra("from_dashboard", true);
            startActivity(intent);
        });

        loadNotices();
    }

    private void showPendingProvidersDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pending Providers");

        RecyclerView rvProviders = new RecyclerView(this);
        rvProviders.setLayoutManager(new LinearLayoutManager(this));
        ProviderAdapter finalAdapter = new ProviderAdapter(new ProviderAdapter.OnAdminActionListener() {
            @Override
            public void onApprove(ServiceProvider provider) {
                approveProvider(provider, (ProviderAdapter) rvProviders.getAdapter());
            }

            @Override
            public void onReject(ServiceProvider provider) {
                rejectProvider(provider, (ProviderAdapter) rvProviders.getAdapter());
            }
        });
        rvProviders.setAdapter(finalAdapter);

        builder.setView(rvProviders);
        builder.setPositiveButton("Close", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        loadPendingProviders(finalAdapter);
    }

    private void loadPendingProviders(ProviderAdapter adapter) {
        // First try Firestore
        firestore.collection("service_providers")
                .whereEqualTo("isApproved", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ServiceProvider> pendingList = queryDocumentSnapshots.toObjects(ServiceProvider.class);
                    if (pendingList.isEmpty()) {
                        // Fallback/Sync from Room
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            List<ServiceProvider> roomPending = providerDao.getPendingProviders();
                            runOnUiThread(() -> {
                                adapter.setProviders(roomPending != null ? roomPending : new java.util.ArrayList<>());
                            });
                        });
                    } else {
                        adapter.setProviders(pendingList);
                        // Sync to Room
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            for (ServiceProvider p : pendingList) providerDao.insert(p);
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        List<ServiceProvider> roomPending = providerDao.getPendingProviders();
                        runOnUiThread(() -> adapter.setProviders(roomPending != null ? roomPending : new java.util.ArrayList<>()));
                    });
                });
    }

    private void approveProvider(ServiceProvider provider, ProviderAdapter adapter) {
        provider.isApproved = true;
        provider.approvalTime = System.currentTimeMillis();

        firestore.collection("service_providers")
                .document(provider.id)
                .set(provider)
                .addOnSuccessListener(aVoid -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        providerDao.updateApprovalStatus(provider.id, true, provider.approvalTime);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Provider Approved!", Toast.LENGTH_SHORT).show();
                            com.example.localconnect.util.NotificationUtil.showApprovalNotification(this, "Provider Approved",
                                    "Approved " + provider.name);
                            if (adapter != null) loadPendingProviders(adapter);
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Approval failed (Firestore): " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void rejectProvider(ServiceProvider provider, ProviderAdapter adapter) {
        firestore.collection("service_providers")
                .document(provider.id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        providerDao.delete(provider);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Provider Rejected (Deleted)", Toast.LENGTH_SHORT).show();
                            if (adapter != null) loadPendingProviders(adapter);
                        });
                    });
                });
    }

    private void showUsersDialog() {
        firestore.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<com.example.localconnect.model.User> users = queryDocumentSnapshots.toObjects(com.example.localconnect.model.User.class);
                    if (users.isEmpty()) {
                        Toast.makeText(this, "No users found in Firestore.", Toast.LENGTH_SHORT).show();
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
                            .setTitle("Registered Users (Cloud)")
                            .setMessage(sb.toString())
                            .setPositiveButton("Close", null)
                            .show();
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
                postNotice(content);
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
        String id = java.util.UUID.randomUUID().toString();
        Notice notice = new Notice(id, "Announcement", content, "GLOBAL", null,
                System.currentTimeMillis());

        firestore.collection("notices")
                .document(id)
                .set(notice)
                .addOnSuccessListener(aVoid -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        noticeDao.insert(notice);
                        
                        // Broadcast SMS logic
                        if (checkSmsPermission()) {
                            try {
                                List<String> phoneNumbers = userDao.getAllUserPhones();
                                android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
                                for (String phone : phoneNumbers) {
                                    if (phone != null && !phone.isEmpty()) {
                                        smsManager.sendTextMessage(phone, null, "LocalConnect: " + content, null, null);
                                    }
                                }
                            } catch (Exception e) { e.printStackTrace(); }
                        }

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Announcement Posted & Cloud Synced", Toast.LENGTH_SHORT).show();
                            loadNotices();
                        });
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to post notice: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
        firestore.collection("notices")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notice> notices = queryDocumentSnapshots.toObjects(Notice.class);
                    if (!notices.isEmpty()) {
                        adapter.setNotices(notices);
                        // Sync to Room
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            for (Notice n : notices) noticeDao.insert(n);
                        });
                    }
                });
    }
}
