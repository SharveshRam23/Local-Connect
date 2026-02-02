package com.example.localconnect.ui.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.model.Notice;

import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {

    private List<Notice> notices;

    public NoticeAdapter(List<Notice> notices) {
        this.notices = notices;
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
        holder.tvNoticeTitle.setText(notice.getTitle());
        holder.tvNoticeDescription.setText(notice.getDescription());

    }

    @Override
    public int getItemCount() {
        return notices.size();
    }

    static class NoticeViewHolder extends RecyclerView.ViewHolder {
        TextView tvNoticeTitle;
        TextView tvNoticeDescription;

        public NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNoticeTitle = itemView.findViewById(R.id.tvNoticeTitle);
            tvNoticeDescription = itemView.findViewById(R.id.tvNoticeDescription);
        }
    }
}