package com.example.localconnect.ui.admin;



import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.model.Issue;
import com.example.localconnect.viewmodel.IssueViewModel;

import java.util.ArrayList;
import java.util.List;

public class ViewIssuesActivity extends AppCompatActivity {

    private IssueViewModel issueViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_issues);

        RecyclerView rvIssues = findViewById(R.id.rvIssues);
        rvIssues.setLayoutManager(new LinearLayoutManager(this));

        // Dummy data for now
        List<Issue> issues = new ArrayList<>();

        IssueAdapter adapter = new IssueAdapter(issues);
        rvIssues.setAdapter(adapter);

        issueViewModel = new ViewModelProvider(this).get(IssueViewModel.class);
        issueViewModel.getAllIssues().observe(this, issueList -> {
            adapter.setIssues(issueList);
        });
    }
}
