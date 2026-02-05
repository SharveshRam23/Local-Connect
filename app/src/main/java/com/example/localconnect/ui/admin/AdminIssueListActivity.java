package com.example.localconnect.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.Issue;
import com.example.localconnect.ui.adapter.IssueAdapter;
import java.util.List;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AdminIssueListActivity extends AppCompatActivity {

    private RecyclerView rvIssues;
    private IssueAdapter adapter;

    @javax.inject.Inject
    com.example.localconnect.data.dao.IssueDao issueDao;

    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_issue_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Reported Issues (Cloud)");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvIssues = findViewById(R.id.rvIssues);
        rvIssues.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new IssueAdapter(issue -> {
            Intent intent = new Intent(this, AdminIssueDetailActivity.class);
            intent.putExtra("issue_id", issue.id);
            startActivity(intent);
        });
        rvIssues.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllIssues();
    }

    private void loadAllIssues() {
        firestore.collection("issues")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Issue> issues = queryDocumentSnapshots.toObjects(Issue.class);
                    if (!issues.isEmpty()) {
                        adapter.setIssues(issues);
                        // Sync to Room
                        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                            for (Issue i : issues) issueDao.insert(i);
                        });
                    } else {
                        // Fallback to local
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            List<Issue> localIssues = issueDao.getAllIssues();
                            runOnUiThread(() -> {
                                adapter.setIssues(localIssues != null ? localIssues : new java.util.ArrayList<>());
                            });
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        List<Issue> localIssues = issueDao.getAllIssues();
                        runOnUiThread(() -> adapter.setIssues(localIssues != null ? localIssues : new java.util.ArrayList<>()));
                    });
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
