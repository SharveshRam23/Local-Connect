package com.example.localconnect.ui.admin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.localconnect.databinding.ActivityViewIssuesBinding;
import com.example.localconnect.viewmodel.IssueViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ViewIssuesActivity extends AppCompatActivity {

    private ActivityViewIssuesBinding binding;
    private IssueViewModel issueViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewIssuesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.rvIssues.setLayoutManager(new LinearLayoutManager(this));

        IssueAdapter adapter = new IssueAdapter(new ArrayList<>());
        binding.rvIssues.setAdapter(adapter);

        issueViewModel = new ViewModelProvider(this).get(IssueViewModel.class);
        issueViewModel.getIssuesByArea("dummy").observe(this, adapter::setIssues);
    }
}
