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

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
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
    private Button btnUpdate, btnForward;
    // Forwarding Info Views
    private View cvForwardingInfo;
    private TextView tvForwardedDept, tvForwardedOfficer, tvForwardedDate;

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
        btnForward = findViewById(R.id.btnForwardIssue);
        
        cvForwardingInfo = findViewById(R.id.cvForwardingInfo);
        tvForwardedDept = findViewById(R.id.tvForwardedDept);
        tvForwardedOfficer = findViewById(R.id.tvForwardedOfficer);
        tvForwardedDate = findViewById(R.id.tvForwardedDate);

        loadIssueDetails();

        btnUpdate.setOnClickListener(v -> updateIssue());
        btnForward.setOnClickListener(v -> showForwardDialog());
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

        if (currentIssue.isForwarded) {
            cvForwardingInfo.setVisibility(View.VISIBLE);
            tvForwardedDept.setText("Dept: " + currentIssue.forwardedDepartment);
            tvForwardedOfficer.setText("Officer: " + currentIssue.forwardedOfficerName);
            tvForwardedDate.setText("Date: " + new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(new java.util.Date(currentIssue.forwardedDate)));
            btnForward.setText("Update Forwarding Details");
        } else {
            cvForwardingInfo.setVisibility(View.GONE);
            btnForward.setText("Forward to Authority");
        }
    }

    private void showForwardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forward to Authority");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_forward_issue, null);
        builder.setView(view);

        Spinner spinner = view.findViewById(R.id.spinnerDepartment);
        EditText etOfficer = view.findViewById(R.id.etOfficerName);
        EditText etContact = view.findViewById(R.id.etContactNumber);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etNote = view.findViewById(R.id.etAdminNote);

        if (currentIssue.isForwarded) {
            // Pre-fill if editing
            etOfficer.setText(currentIssue.forwardedOfficerName);
            etContact.setText(currentIssue.forwardedContact);
            etEmail.setText(currentIssue.forwardedEmail);
            etNote.setText(currentIssue.adminNote);
        }

        builder.setPositiveButton("Forward", (dialog, which) -> {
            String dept = spinner.getSelectedItem().toString();
            String officer = etOfficer.getText().toString().trim();
            String contact = etContact.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            if (officer.isEmpty() || contact.isEmpty()) {
                Toast.makeText(this, "Officer Name and Contact are required", Toast.LENGTH_SHORT).show();
                return;
            }

            forwardIssue(dept, officer, contact, email, note);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void forwardIssue(String dept, String officer, String contact, String email, String note) {
        currentIssue.isForwarded = true;
        currentIssue.forwardedDepartment = dept;
        currentIssue.forwardedOfficerName = officer;
        currentIssue.forwardedContact = contact;
        currentIssue.forwardedEmail = email;
        currentIssue.adminNote = note;
        currentIssue.forwardedDate = System.currentTimeMillis();
        currentIssue.status = "FORWARDED_TO_AUTHORITY"; // Custom status not in enum yet, handled as string

        firestore.collection("issues").document(issueId).set(currentIssue)
                .addOnSuccessListener(aVoid -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        issueDao.insert(currentIssue); // Full update
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Issue Forwarded Successfully", Toast.LENGTH_SHORT).show();
                            populateUI();
                            
                            // Ask to send Email/SMS
                            if (!email.isEmpty()) {
                                sendEmailIntent(email, dept, officer, note);
                            } else {
                                sendSmsIntent(contact, dept);
                            }
                        });
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to forward: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void sendEmailIntent(String email, String dept, String officer, String note) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Civic Issue Forwarded: " + currentIssue.id);
        intent.putExtra(Intent.EXTRA_TEXT, "Dear " + officer + " (" + dept + "),\n\n" +
                "A civic issue has been reported in your jurisdiction.\n\n" +
                "Description: " + currentIssue.description + "\n" +
                "Location Pincode: " + currentIssue.pincode + "\n" +
                "Admin Note: " + note + "\n\n" +
                "Please take necessary action.\n\nLocalConnect Admin");
        
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void sendSmsIntent(String contact, String dept) {
         Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", contact, null));
         intent.putExtra("sms_body", "LocalConnect Alert: New Issue forwarded to " + dept + ". ID: " + currentIssue.id + ". Please check portal.");
         if (intent.resolveActivity(getPackageManager()) != null) {
             startActivity(intent);
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
