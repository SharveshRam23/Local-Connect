package com.example.localconnect.ui.issue;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.Issue;
import com.example.localconnect.ui.adapter.IssueAdapter;

import com.example.localconnect.ui.adapter.IssueAdapter;
import java.util.List;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NearbyIssuesActivity extends AppCompatActivity {

    private RecyclerView rvIssues;
    private IssueAdapter adapter;

    @javax.inject.Inject
    com.example.localconnect.data.dao.IssueDao issueDao;

    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_issues);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Nearby Issues (Cloud)");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvIssues = findViewById(R.id.rvIssues);
        rvIssues.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new IssueAdapter(issue -> {
            android.content.Intent intent = new android.content.Intent(this, com.example.localconnect.ui.user.UserIssueDetailActivity.class);
            intent.putExtra("issue_id", issue.id);
            startActivity(intent);
        });
        rvIssues.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        loadNearbyIssues();
    }

    private void loadNearbyIssues() {
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        String pincode = prefs.getString("user_pincode", "000000");

        firestore.collection("issues")
                .whereEqualTo("pincode", pincode)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Issue> issues = queryDocumentSnapshots.toObjects(Issue.class);
                    if (!issues.isEmpty()) {
                        adapter.setIssues(issues);
                        // Sync to local
                        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                            for (Issue i : issues) issueDao.insert(i);
                        });
                    } else {
                        // Fallback to local
                        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                            List<Issue> localIssues = issueDao.getIssuesByPincode(pincode);
                            runOnUiThread(() -> {
                                if (localIssues != null && !localIssues.isEmpty()) {
                                    adapter.setIssues(localIssues);
                                } else {
                                    Toast.makeText(this, "No issues in your area.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    // Firestore fail fallback
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        List<Issue> localIssues = issueDao.getIssuesByPincode(pincode);
                        runOnUiThread(() -> {
                            if (localIssues != null && !localIssues.isEmpty()) {
                                adapter.setIssues(localIssues);
                            } else {
                                Toast.makeText(this, "Cloud Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
