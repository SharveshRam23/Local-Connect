package com.example.localconnect.ui.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.localconnect.R;
import com.example.localconnect.model.Notice;
import com.example.localconnect.viewmodel.NoticeViewModel;

public class AddNoticeActivity extends AppCompatActivity {

    private EditText etNoticeTitle, etNoticeDescription;
    private Button btnAddNotice;
    private NoticeViewModel noticeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notice);

        noticeViewModel = new ViewModelProvider(this).get(NoticeViewModel.class);

        etNoticeTitle = findViewById(R.id.etNoticeTitle);
        etNoticeDescription = findViewById(R.id.etNoticeDescription);
        btnAddNotice = findViewById(R.id.btnAddNotice);

        btnAddNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etNoticeTitle.getText()) || TextUtils.isEmpty(etNoticeDescription.getText())) {
                    Toast.makeText(AddNoticeActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                } else {
                    String title = etNoticeTitle.getText().toString();
                    String description = etNoticeDescription.getText().toString();
                    Notice notice = new Notice(title, description, "GLOBAL", "", System.currentTimeMillis());
                    noticeViewModel.insert(notice);
                    finish();
                }
            }
        });
    }
}
