
package com.example.localconnect.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.localconnect.R;

public class AdminHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        Button btnViewPendingRequests = findViewById(R.id.btnViewPendingRequests);
        Button btnAddNotice = findViewById(R.id.btnAddNotice);
        Button btnViewIssues = findViewById(R.id.btnViewIssues);

        btnViewPendingRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminHomeActivity.this, ViewPendingRequestsActivity.class));
            }
        });

        btnAddNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminHomeActivity.this, AddNoticeActivity.class));
            }
        });

        btnViewIssues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminHomeActivity.this, ViewIssuesActivity.class));
            }
        });
    }
}
