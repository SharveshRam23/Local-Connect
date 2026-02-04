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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_issues);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Nearby Issues");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvIssues = findViewById(R.id.rvIssues);
        rvIssues.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new IssueAdapter(issue -> {
            // Optional: View detail
        });
        rvIssues.setAdapter(adapter);

        loadNearbyIssues();
    }

    private void loadNearbyIssues() {
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        String pincode = prefs.getString("user_pincode", "000000");

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Issue> issues = AppDatabase.getDatabase(getApplicationContext())
                    .issueDao().getIssuesByPincode(pincode);
            
            runOnUiThread(() -> {
                if (issues == null || issues.isEmpty()) {
                    Toast.makeText(this, "No issues reported in your area.", Toast.LENGTH_SHORT).show();
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
