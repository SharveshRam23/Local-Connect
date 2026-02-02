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

public class ServiceProviderAdapter extends RecyclerView.Adapter<ServiceProviderAdapter.ViewHolder> {

    private List<ServiceProvider> serviceProviders;

    public ServiceProviderAdapter(List<ServiceProvider> serviceProviders) {
        this.serviceProviders = serviceProviders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service_provider, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceProvider serviceProvider = serviceProviders.get(position);
        holder.tvProviderName.setText(serviceProvider.getServiceType());
        holder.tvProviderService.setText(serviceProvider.getPhone());
        holder.tvProviderLocation.setText(serviceProvider.getArea());
    }

    @Override
    public int getItemCount() {
        return serviceProviders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProviderName, tvProviderService, tvProviderLocation;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProviderName = itemView.findViewById(R.id.tvProviderName);
            tvProviderService = itemView.findViewById(R.id.tvProviderService);
            tvProviderLocation = itemView.findViewById(R.id.tvProviderLocation);
        }
    }
}
