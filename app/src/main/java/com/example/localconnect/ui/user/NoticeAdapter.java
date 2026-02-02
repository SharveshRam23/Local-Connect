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

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.ViewHolder> {

    private List<Notice> notices;

    public NoticeAdapter(List<Notice> notices) {
        this.notices = notices;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notice notice = notices.get(position);
        holder.tvNoticeTitle.setText(notice.getTitle());
        holder.tvNoticeDescription.setText(notice.getMessage());
    }

    @Override
    public int getItemCount() {
        return notices.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNoticeTitle, tvNoticeDescription;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNoticeTitle = itemView.findViewById(R.id.tvNoticeTitle);
            tvNoticeDescription = itemView.findViewById(R.id.tvNoticeDescription);
        }
    }
}
