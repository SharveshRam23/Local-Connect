package com.example.localconnect.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.model.ServiceProvider;

import java.util.ArrayList;
import java.util.List;

public class ProviderAdapter extends RecyclerView.Adapter<ProviderAdapter.ProviderViewHolder> {

    private List<ServiceProvider> providers = new ArrayList<>();
    private OnProviderClickListener clickListener;
    private OnAdminActionListener adminListener;
    private boolean isAdminMode = false;

    public interface OnProviderClickListener {
        void onProviderClick(ServiceProvider provider);
    }

    public interface OnAdminActionListener {
        void onApprove(ServiceProvider provider);
        void onReject(ServiceProvider provider);
    }

    public ProviderAdapter(OnProviderClickListener listener) {
        this.clickListener = listener;
        this.isAdminMode = false;
    }

    public ProviderAdapter(OnAdminActionListener listener) {
        this.adminListener = listener;
        this.isAdminMode = true;
    }

    public void setProviders(List<ServiceProvider> providers) {
        if (providers == null) {
            this.providers = new ArrayList<>();
        } else {
            this.providers = providers;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProviderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isAdminMode ? R.layout.item_provider_admin : R.layout.item_provider;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ProviderViewHolder(view, isAdminMode);
    }

    @Override
    public void onBindViewHolder(@NonNull ProviderViewHolder holder, int position) {
        ServiceProvider provider = providers.get(position);
        holder.tvName.setText(provider.name);
        holder.tvCategory.setText("Category: " + provider.category);
        holder.tvExperience.setText("Experience: " + provider.experience + " Years");
        holder.tvPhone.setText("Phone: " + provider.phone);
        holder.tvPincode.setText("Service Area: " + provider.pincode);

        if (provider.isAvailable) {
            holder.tvPincode.append("  ● Available");
            holder.tvPincode.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // Green
        } else {
            holder.tvPincode.append("  ● Not Available");
            holder.tvPincode.setTextColor(android.graphics.Color.RED);
        }

        if (isAdminMode) {
            holder.btnApprove.setOnClickListener(v -> {
                if (adminListener != null) adminListener.onApprove(provider);
            });
            holder.btnReject.setOnClickListener(v -> {
                if (adminListener != null) adminListener.onReject(provider);
            });
        } else {
            holder.itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onProviderClick(provider);
            });
        }
    }

    @Override
    public int getItemCount() {
        return providers.size();
    }

    static class ProviderViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvExperience, tvPhone, tvPincode;
        Button btnApprove, btnReject;

        public ProviderViewHolder(@NonNull View itemView, boolean isAdmin) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProviderName);
            tvCategory = itemView.findViewById(R.id.tvProviderCategory);
            tvExperience = itemView.findViewById(R.id.tvProviderExperience);
            tvPhone = itemView.findViewById(R.id.tvProviderPhone);
            tvPincode = itemView.findViewById(R.id.tvProviderPincode);
            if (isAdmin) {
                btnApprove = itemView.findViewById(R.id.btnApprove);
                btnReject = itemView.findViewById(R.id.btnReject);
            }
        }
    }
}
