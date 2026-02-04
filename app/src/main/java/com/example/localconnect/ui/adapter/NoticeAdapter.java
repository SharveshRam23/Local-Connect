package com.example.localconnect.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.model.Notice;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {

    private List<Notice> notices = new ArrayList<>();

    public void setNotices(List<Notice> notices) {
        if (notices == null) {
            this.notices = new ArrayList<>();
        } else {
            this.notices = notices;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice, parent, false);
        return new NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        Notice notice = notices.get(position);
        holder.bind(notice);
    }

    @Override
    public int getItemCount() {
        return notices.size();
    }

    static class NoticeViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvContent;
        private final TextView tvTime;

        public NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNoticeTitle);
            tvContent = itemView.findViewById(R.id.tvNoticeContent);
            tvTime = itemView.findViewById(R.id.tvNoticeTime);
        }

        public void bind(Notice notice) {
            tvTitle.setText(notice.title);
            tvContent.setText(notice.content);

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            tvTime.setText(sdf.format(new Date(notice.scheduledTime)));

            itemView.setOnClickListener(v -> {
                android.content.Context context = v.getContext();
                android.content.Intent intent = new android.content.Intent(context, com.example.localconnect.ui.user.NoticeDetailActivity.class);
                intent.putExtra("notice_id", notice.id);
                intent.putExtra("notice_title", notice.title);
                intent.putExtra("notice_content", notice.content);
                intent.putExtra("notice_time", notice.scheduledTime);
                context.startActivity(intent);
            });
        }
    }
}
