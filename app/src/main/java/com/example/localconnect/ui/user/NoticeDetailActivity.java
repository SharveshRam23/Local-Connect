package com.example.localconnect.ui.user;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.databinding.ActivityNoticeDetailBinding;
import com.example.localconnect.model.Comment;
import com.example.localconnect.model.Notice;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NoticeDetailActivity extends AppCompatActivity {

    private ActivityNoticeDetailBinding binding;
    private String noticeId;
    private String noticeTitle;
    private String noticeContent;
    private long noticeTime;

    @javax.inject.Inject
    com.example.localconnect.data.dao.CommentDao commentDao;

    private CommentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoticeDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getIntent() != null) {
            noticeId = getIntent().getStringExtra("notice_id");
            noticeTitle = getIntent().getStringExtra("notice_title");
            noticeContent = getIntent().getStringExtra("notice_content");
            noticeTime = getIntent().getLongExtra("notice_time", 0);
        }

        if (noticeId == null) {
            finish();
            return;
        }

        setupUI();
        setupComments();
    }

    private void setupUI() {
        binding.tvDetailTitle.setText(noticeTitle);
        binding.tvDetailContent.setText(noticeContent);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        binding.tvDetailTime.setText(sdf.format(new Date(noticeTime)));

        binding.btnPostComment.setOnClickListener(v -> postComment());
    }

    private void setupComments() {
        binding.rvComments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommentAdapter();
        binding.rvComments.setAdapter(adapter);
        loadComments();
    }

    private void loadComments() {
        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Comment> comments = commentDao.getCommentsForNotice(noticeId);
            runOnUiThread(() -> adapter.setComments(comments));
        });
    }

    private void postComment() {
        String content = binding.etComment.getText().toString().trim();
        if (TextUtils.isEmpty(content)) return;

        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        String userName = prefs.getString("user_name", "Anonymous");

        if (userId == null) {
            Toast.makeText(this, "You must be logged in to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
            Comment comment = new Comment(noticeId, userId, userName, content, System.currentTimeMillis());
            commentDao.insert(comment);
            binding.etComment.setText("");
            loadComments(); // Refresh
        });
    }

    // Inner Adapter Class
    private static class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
        private List<Comment> comments = new ArrayList<>();

        void setComments(List<Comment> comments) {
            this.comments = comments;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
            Comment comment = comments.get(position);
            holder.text1.setText(comment.userName);
            holder.text2.setText(comment.content);
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        static class CommentViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            CommentViewHolder(@NonNull View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
