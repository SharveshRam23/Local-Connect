package com.example.localconnect.ui.admin;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import androidx.appcompat.app.AppCompatActivity;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.Issue;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AdminIssueDetailActivity extends AppCompatActivity {

    private ImageView ivImage;
    private TextView tvReporter, tvDescription, tvPincode;
    private RadioGroup rgStatus;
    private EditText etReply;
    private Button btnUpdate;
    private String issueId;
    private Issue currentIssue;

    @javax.inject.Inject
    com.example.localconnect.data.dao.IssueDao issueDao;

    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_issue_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Issue Details (Cloud)");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        issueId = getIntent().getStringExtra("issue_id");

        ivImage = findViewById(R.id.ivDetailImage);
        tvReporter = findViewById(R.id.tvDetailReporter);
        tvDescription = findViewById(R.id.tvDetailDescription);
        tvPincode = findViewById(R.id.tvDetailPincode);
        rgStatus = findViewById(R.id.rgStatus);
        etReply = findViewById(R.id.etAdminReply);
        btnUpdate = findViewById(R.id.btnUpdateIssue);

        loadIssueDetails();

        btnUpdate.setOnClickListener(v -> updateIssue());
    }

    private void loadIssueDetails() {
        // Try Room first
        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
            currentIssue = issueDao.getIssueById(issueId);
            if (currentIssue != null) {
                runOnUiThread(() -> populateUI());
            }

            // Sync from Firestore
            firestore.collection("issues").document(issueId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Issue issue = documentSnapshot.toObject(Issue.class);
                        if (issue != null) {
                            currentIssue = issue;
                            populateUI();
                            // Update Room
                            com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> issueDao.insert(issue));
                        }
                    });
        });
    }

    private void populateUI() {
        if (currentIssue == null) return;
        
        tvReporter.setText("Reporter: " + currentIssue.reporterName);
        tvDescription.setText(currentIssue.description);
        tvPincode.setText("Pincode: " + currentIssue.pincode);
        etReply.setText(currentIssue.adminResponse);

        if (currentIssue.imagePath != null && !currentIssue.imagePath.isEmpty()) {
            if (currentIssue.imagePath.startsWith("http")) {
                Glide.with(this)
                        .load(currentIssue.imagePath)
                        .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                            @Override
                            public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                Toast.makeText(AdminIssueDetailActivity.this, "Image Not Loaded: " + (e != null ? e.getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(ivImage);
            } else if (currentIssue.imagePath.length() > 500) { // Likely Base64
                ivImage.setImageBitmap(com.example.localconnect.util.ImageUtil.fromBase64(currentIssue.imagePath));
            } else {
                ivImage.setImageBitmap(BitmapFactory.decodeFile(currentIssue.imagePath));
            }
        }

        if ("PENDING".equals(currentIssue.status)) {
            ((RadioButton) findViewById(R.id.rbPending)).setChecked(true);
        } else if ("IN_PROGRESS".equals(currentIssue.status)) {
            ((RadioButton) findViewById(R.id.rbInProgress)).setChecked(true);
        } else if ("RESOLVED".equals(currentIssue.status)) {
            ((RadioButton) findViewById(R.id.rbResolved)).setChecked(true);
        }
    }

    private void updateIssue() {
        if (currentIssue == null) return;

        String reply = etReply.getText().toString().trim();
        int selectedId = rgStatus.getCheckedRadioButtonId();
        String status = "PENDING";
        if (selectedId == R.id.rbInProgress) status = "IN_PROGRESS";
        else if (selectedId == R.id.rbResolved) status = "RESOLVED";

        currentIssue.status = status;
        currentIssue.adminResponse = reply;

        firestore.collection("issues")
                .document(issueId)
                .set(currentIssue)
                .addOnSuccessListener(aVoid -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        issueDao.updateIssueStatus(issueId, currentIssue.status, currentIssue.adminResponse);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Issue updated in cloud!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
