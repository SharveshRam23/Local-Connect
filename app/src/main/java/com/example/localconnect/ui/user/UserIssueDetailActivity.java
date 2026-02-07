package com.example.localconnect.ui.user;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.localconnect.R;
import com.example.localconnect.model.Issue;
import dagger.hilt.android.AndroidEntryPoint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@AndroidEntryPoint
public class UserIssueDetailActivity extends AppCompatActivity {

    private ImageView ivImage;
    private TextView tvStatus, tvDescription, tvAdminResponse;
    private View cvForwardingInfo;
    private TextView tvForwardedDept, tvForwardedOfficer, tvForwardedDate;

    private String issueId;
    
    @javax.inject.Inject
    com.example.localconnect.data.dao.IssueDao issueDao;
    
    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_issue_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Issue Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        issueId = getIntent().getStringExtra("issue_id");

        ivImage = findViewById(R.id.ivUserDetailImage);
        tvStatus = findViewById(R.id.tvUserDetailStatus);
        tvDescription = findViewById(R.id.tvUserDetailDescription);
        tvAdminResponse = findViewById(R.id.tvUserDetailAdminResponse);
        
        cvForwardingInfo = findViewById(R.id.cvUserForwardingInfo);
        tvForwardedDept = findViewById(R.id.tvUserForwardedDept);
        tvForwardedOfficer = findViewById(R.id.tvUserForwardedOfficer);
        tvForwardedDate = findViewById(R.id.tvUserForwardedDate);

        loadIssueDetails();
    }

    private void loadIssueDetails() {
         com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
            Issue issue = issueDao.getIssueById(issueId);
            if (issue != null) {
                runOnUiThread(() -> populateUI(issue));
            } else {
                 // Try fetching from Firestore if null (though it should be passed or synced)
                 firestore.collection("issues").document(issueId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Issue cloudIssue = documentSnapshot.toObject(Issue.class);
                        if (cloudIssue != null) {
                            runOnUiThread(() -> populateUI(cloudIssue));
                        }
                    });
            }
        });
    }

    private void populateUI(Issue issue) {
        tvDescription.setText(issue.description);
        
        if (issue.isForwarded) {
            tvStatus.setText("Forwarded to Authority");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            
            cvForwardingInfo.setVisibility(View.VISIBLE);
            tvForwardedDept.setText("Dept: " + issue.forwardedDepartment);
            tvForwardedOfficer.setText("Officer: " + issue.forwardedOfficerName);
             tvForwardedDate.setText("Date: " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(issue.forwardedDate)));
        } else {
            tvStatus.setText(issue.status);
            cvForwardingInfo.setVisibility(View.GONE);
            if ("RESOLVED".equals(issue.status)) {
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                 tvStatus.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        }

        if (issue.adminResponse != null && !issue.adminResponse.isEmpty()) {
            tvAdminResponse.setText(issue.adminResponse);
        } else {
            tvAdminResponse.setText("No response from admin yet.");
        }

        if (issue.imagePath != null && !issue.imagePath.isEmpty()) {
             if (issue.imagePath.startsWith("http")) {
                Glide.with(this).load(issue.imagePath).into(ivImage);
            } else if (issue.imagePath.length() > 500) {
                ivImage.setImageBitmap(com.example.localconnect.util.ImageUtil.fromBase64(issue.imagePath));
            } else {
                ivImage.setImageBitmap(BitmapFactory.decodeFile(issue.imagePath));
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
