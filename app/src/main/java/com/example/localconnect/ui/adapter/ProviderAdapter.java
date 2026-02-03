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
    private OnProviderActionListener listener;

    public interface OnProviderActionListener {
        void onApprove(ServiceProvider provider);

        void onReject(ServiceProvider provider);
    }

    public void setProviders(List<ServiceProvider> providers, OnProviderActionListener listener) {
        this.providers = providers;
        this.listener = listener;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProviderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_provider_admin, parent, false);
        return new ProviderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProviderViewHolder holder, int position) {
        ServiceProvider provider = providers.get(position);
        holder.tvName.setText(provider.name);
        holder.tvCategory.setText("Category: " + provider.category);
        holder.tvExperience.setText("Experience: " + provider.experience + " Years");
        holder.tvPhone.setText("Phone: " + provider.phone);

        holder.btnApprove.setOnClickListener(v -> {
            if (listener != null)
                listener.onApprove(provider);
        });

        holder.btnReject.setOnClickListener(v -> {
            if (listener != null)
                listener.onReject(provider);
        });
    }

    @Override
    public int getItemCount() {
        return providers.size();
    }

    static class ProviderViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvExperience, tvPhone;
        Button btnApprove, btnReject;

        public ProviderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProviderName);
            tvCategory = itemView.findViewById(R.id.tvProviderCategory);
            tvExperience = itemView.findViewById(R.id.tvProviderExperience);
            tvPhone = itemView.findViewById(R.id.tvProviderPhone);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
