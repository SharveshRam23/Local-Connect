package com.example.localconnect.ui.adapter;

import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.model.Issue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IssueAdapter extends RecyclerView.Adapter<IssueAdapter.IssueViewHolder> {

    private boolean isAdmin = false;
    private List<Issue> issues = new java.util.ArrayList<>();
    private OnIssueClickListener clickListener;

    public interface OnIssueClickListener {
        void onIssueClick(Issue issue);
    }

    public IssueAdapter(OnIssueClickListener listener) {
        this.clickListener = listener;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IssueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = isAdmin ? R.layout.item_issue_admin : R.layout.item_issue;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new IssueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IssueViewHolder holder, int position) {
        Issue issue = issues.get(position);
        holder.tvReporter.setText("Reporter: " + (issue.reporterName != null ? issue.reporterName : "Anonymous"));
        holder.tvDescription.setText(issue.description);
        holder.tvStatus.setText("Status: " + issue.status);
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(issue.timestamp)));

        if (issue.imagePath != null && !issue.imagePath.isEmpty()) {
            if (issue.imagePath.length() > 200) { // Likely a Base64 string
                android.graphics.Bitmap bitmap = com.example.localconnect.util.ImageUtil.fromBase64(issue.imagePath);
                if (bitmap != null) {
                    holder.ivIssue.setImageBitmap(bitmap);
                }
            } else {
                // Use ImageUtil to decode safely (resizing to thumbnail size for list)
                holder.ivIssue.setImageBitmap(com.example.localconnect.util.ImageUtil.decodeSampledBitmapFromPath(issue.imagePath, 200, 200));
            }
        }

        if (holder.llAdminResponse != null && holder.tvAdminComment != null) {
            if (issue.adminResponse != null && !issue.adminResponse.isEmpty()) {
                holder.llAdminResponse.setVisibility(View.VISIBLE);
                holder.tvAdminComment.setText(issue.adminResponse);
            } else {
                holder.llAdminResponse.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onIssueClick(issue);
        });
    }

    @Override
    public int getItemCount() {
        return issues.size();
    }

    static class IssueViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIssue;
        TextView tvReporter, tvDescription, tvStatus, tvDate, tvAdminComment;
        LinearLayout llAdminResponse;

        public IssueViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIssue = itemView.findViewById(R.id.ivIssue);
            tvReporter = itemView.findViewById(R.id.tvReporter);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAdminComment = itemView.findViewById(R.id.tvAdminComment);
            llAdminResponse = itemView.findViewById(R.id.llAdminResponse);
        }
    }
}
