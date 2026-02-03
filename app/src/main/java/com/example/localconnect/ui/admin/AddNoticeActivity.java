package com.example.localconnect.ui.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.localconnect.databinding.ActivityAddNoticeBinding;
import com.example.localconnect.model.Notice;
import com.example.localconnect.viewmodel.NoticeViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddNoticeActivity extends AppCompatActivity {

    private ActivityAddNoticeBinding binding;
    private NoticeViewModel noticeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddNoticeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        noticeViewModel = new ViewModelProvider(this).get(NoticeViewModel.class);

        binding.btnAddNotice.setOnClickListener(v -> {
            String title = binding.etNoticeTitle.getText().toString();
            String description = binding.etNoticeDescription.getText().toString();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description)) {
                Toast.makeText(AddNoticeActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else {
                Notice notice = new Notice(title, description, "GLOBAL", System.currentTimeMillis());
                noticeViewModel.insert(notice);
                finish();
            }
        });
    }
}
