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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_issue_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Reported Issues");
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
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Issue> issues = AppDatabase.getDatabase(getApplicationContext())
                    .issueDao().getAllIssues();
            
            runOnUiThread(() -> {
                if (issues == null || issues.isEmpty()) {
                    Toast.makeText(this, "No issues reported yet.", Toast.LENGTH_SHORT).show();
                    adapter.setIssues(new java.util.ArrayList<>());
                } else {
                    adapter.setIssues(issues);
                }
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
