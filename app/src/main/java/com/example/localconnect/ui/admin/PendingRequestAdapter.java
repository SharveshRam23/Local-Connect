package com.example.localconnect.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.model.ServiceProvider;

import java.util.List;

public class PendingRequestAdapter extends RecyclerView.Adapter<PendingRequestAdapter.ViewHolder> {

    private List<ServiceProvider> pendingRequests;

    public PendingRequestAdapter(List<ServiceProvider> pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pending_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceProvider provider = pendingRequests.get(position);
        holder.tvProviderName.setText(provider.getName());

        holder.btnApprove.setOnClickListener(v -> {
            // Handle approve logic
        });

        holder.btnDeny.setOnClickListener(v -> {
            // Handle deny logic
        });
    }

    @Override
    public int getItemCount() {
        return pendingRequests.size();
    }

    public void setPendingRequests(List<ServiceProvider> pendingRequests) {
        this.pendingRequests = pendingRequests;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProviderName;
        Button btnApprove, btnDeny;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProviderName = itemView.findViewById(R.id.tvProviderName);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnDeny = itemView.findViewById(R.id.btnDeny);
        }
    }
}
