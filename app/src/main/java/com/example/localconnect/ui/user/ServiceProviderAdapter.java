package com.example.localconnect.ui.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.model.ServiceProvider;

import java.util.List;

public class ServiceProviderAdapter extends RecyclerView.Adapter<ServiceProviderAdapter.ServiceProviderViewHolder> {

    private List<ServiceProvider> serviceProviders;

    public ServiceProviderAdapter(List<ServiceProvider> serviceProviders) {
        this.serviceProviders = serviceProviders;
    }

    @NonNull
    @Override
    public ServiceProviderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_provider, parent, false);
        return new ServiceProviderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceProviderViewHolder holder, int position) {
        ServiceProvider serviceProvider = serviceProviders.get(position);
        holder.tvProviderName.setText(serviceProvider.getName());
        holder.tvProviderService.setText(serviceProvider.getCategory());
        holder.tvProviderLocation.setText(serviceProvider.getAddress());
    }

    @Override
    public int getItemCount() {
        return serviceProviders.size();
    }

    static class ServiceProviderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProviderName, tvProviderService, tvProviderLocation;

        public ServiceProviderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProviderName = itemView.findViewById(R.id.tvProviderName);
            tvProviderService = itemView.findViewById(R.id.tvProviderService);
            tvProviderLocation = itemView.findViewById(R.id.tvProviderLocation);
        }
    }
}
