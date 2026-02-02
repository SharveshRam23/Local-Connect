
package com.example.localconnect.ui.admin;

import android.os.Bundle;
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
    private NoticeViewModel noticeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notice);

        etNoticeTitle = findViewById(R.id.etNoticeTitle);
        etNoticeDescription = findViewById(R.id.etNoticeDescription);
        Button btnAddNotice = findViewById(R.id.btnAddNotice);

        noticeViewModel = new ViewModelProvider(this).get(NoticeViewModel.class);

        btnAddNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = etNoticeTitle.getText().toString().trim();
                String description = etNoticeDescription.getText().toString().trim();

                if (!title.isEmpty() && !description.isEmpty()) {
                    Notice notice = new Notice(title, description);
                    noticeViewModel.insert(notice);
                    Toast.makeText(AddNoticeActivity.this, "Notice added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddNoticeActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
