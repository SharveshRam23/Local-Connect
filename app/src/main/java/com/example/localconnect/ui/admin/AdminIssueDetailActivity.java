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
    private int issueId;
    private Issue currentIssue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_issue_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Issue Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        issueId = getIntent().getIntExtra("issue_id", -1);

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
        AppDatabase.databaseWriteExecutor.execute(() -> {
            currentIssue = AppDatabase.getDatabase(getApplicationContext()).issueDao().getIssueById(issueId);
            runOnUiThread(() -> {
                if (currentIssue != null) {
                    tvReporter.setText("Reporter: " + currentIssue.reporterName);
                    tvDescription.setText(currentIssue.description);
                    tvPincode.setText("Pincode: " + currentIssue.pincode);
                    etReply.setText(currentIssue.adminResponse);

                    if (currentIssue.imagePath != null && !currentIssue.imagePath.isEmpty()) {
                        ivImage.setImageBitmap(BitmapFactory.decodeFile(currentIssue.imagePath));
                    }

                    if ("PENDING".equals(currentIssue.status)) {
                        ((RadioButton) findViewById(R.id.rbPending)).setChecked(true);
                    } else if ("IN_PROGRESS".equals(currentIssue.status)) {
                        ((RadioButton) findViewById(R.id.rbInProgress)).setChecked(true);
                    } else if ("RESOLVED".equals(currentIssue.status)) {
                        ((RadioButton) findViewById(R.id.rbResolved)).setChecked(true);
                    }
                }
            });
        });
    }

    private void updateIssue() {
        String reply = etReply.getText().toString().trim();
        int selectedId = rgStatus.getCheckedRadioButtonId();
        String status = "PENDING";
        if (selectedId == R.id.rbInProgress) status = "IN_PROGRESS";
        else if (selectedId == R.id.rbResolved) status = "RESOLVED";

        final String finalStatus = status;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getDatabase(getApplicationContext()).issueDao().updateIssueStatus(issueId, finalStatus, reply);
            runOnUiThread(() -> {
                Toast.makeText(this, "Issue updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
