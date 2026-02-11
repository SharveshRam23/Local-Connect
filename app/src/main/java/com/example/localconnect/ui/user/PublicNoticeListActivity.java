package com.example.localconnect.ui.user;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.Notice;
import com.example.localconnect.ui.adapter.NoticeAdapter;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PublicNoticeListActivity extends AppCompatActivity {

    @javax.inject.Inject
    com.example.localconnect.data.dao.NoticeDao noticeDao;

    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    private RecyclerView rvNotices;
    private NoticeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_notice_list);

        // Header Back Button
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        rvNotices = findViewById(R.id.rvAllNotices);
        rvNotices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoticeAdapter();
        adapter.setIsAdmin(false); // Read-only
        rvNotices.setAdapter(adapter);

        loadNotices();
    }

    private void loadNotices() {
        // Logic similar to UserHomeActivity but loads ALL
        // We might want to filter by Pincode if that's a requirement, 
        // but "Community Notices" often implies general ones too.
        // For now, let's load all like Admin but maybe filter locally if needed?
        // UserHomeActivity filters by user pincode for fallback.
        // Let's try to stick to consistent logic: Load Global + Targeted.

        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        // Check if User or Provider to get pincode
        String pincode = prefs.getString("user_pincode", "");
        if (pincode.isEmpty()) {
             pincode = prefs.getString("provider_pincode", "");
        }
        
        final String finalPincode = pincode;

        firestore.collection("notices")
                .orderBy("scheduledTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notice> notices = queryDocumentSnapshots.toObjects(Notice.class);
                    if (!notices.isEmpty()) {
                        adapter.setNotices(notices); // Showing all for now, can filter if needed
                         // Sync to Room
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            for (Notice n : notices) noticeDao.insert(n);
                        });
                    }
                })
                .addOnFailureListener(e -> {
                     // Fallback to local
                     if (!finalPincode.isEmpty()) {
                         AppDatabase.databaseWriteExecutor.execute(() -> {
                             List<Notice> localNotices = noticeDao.getNoticesForUser(finalPincode); // Assuming this returns relevant ones
                             runOnUiThread(() -> adapter.setNotices(localNotices));
                         });
                     } else {
                         Toast.makeText(this, "Could not load notices", Toast.LENGTH_SHORT).show();
                     }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.releaseMediaPlayer();
        }
    }
}
