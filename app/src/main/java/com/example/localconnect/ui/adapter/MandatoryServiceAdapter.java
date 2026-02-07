package com.example.localconnect.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.model.MandatoryService;
import com.example.localconnect.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;

public class MandatoryServiceAdapter extends RecyclerView.Adapter<MandatoryServiceAdapter.ServiceViewHolder> {

    private List<MandatoryService> services = new ArrayList<>();
    private boolean isAdmin = false;
    private OnServiceClickListener listener;

    public interface OnServiceClickListener {
        void onServiceClick(MandatoryService service);
        void onEditClick(MandatoryService service);
        void onDeleteClick(MandatoryService service);
    }

    public void setOnServiceClickListener(OnServiceClickListener listener) {
        this.listener = listener;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
        notifyDataSetChanged();
    }

    public void setServices(List<MandatoryService> services) {
        this.services = services != null ? services : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mandatory_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        MandatoryService service = services.get(position);
        holder.bind(service);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvAddress, tvHours, tvPincode, tvStatusBadge;
        ImageView ivImage, ivEmergencyTag;
        View adminActions;
        View btnEdit, btnDelete;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvServiceName);
            tvCategory = itemView.findViewById(R.id.tvServiceCategory);
            tvAddress = itemView.findViewById(R.id.tvServiceAddress);
            tvHours = itemView.findViewById(R.id.tvWorkingHours);
            tvPincode = itemView.findViewById(R.id.tvPincode);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            ivImage = itemView.findViewById(R.id.ivServiceImage);
            ivEmergencyTag = itemView.findViewById(R.id.ivEmergencyTag);
            adminActions = itemView.findViewById(R.id.adminActionButtons);
            btnEdit = itemView.findViewById(R.id.btnEditService);
            btnDelete = itemView.findViewById(R.id.btnDeleteService);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onServiceClick(services.get(pos));
                }
            });

            btnEdit.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEditClick(services.get(pos));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeleteClick(services.get(pos));
                }
            });
        }

        public void bind(MandatoryService service) {
            tvName.setText(service.name);
            tvCategory.setText(service.category);
            tvAddress.setText(service.address);
            tvPincode.setText(service.pincode);
            
            String hoursText = service.is24x7 ? "24x7 Available" : service.workingHours;
            tvHours.setText(hoursText);

            ivEmergencyTag.setVisibility(service.isEmergency ? View.VISIBLE : View.GONE);
            adminActions.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

            // Simple "Open/Closed" logic (could be more complex)
            if (service.is24x7) {
                tvStatusBadge.setText("OPEN 24x7");
                tvStatusBadge.setBackgroundColor(0xFF4CAF50); // Green
            } else {
                tvStatusBadge.setText("ACTIVE");
                tvStatusBadge.setBackgroundColor(0xFF2196F3); // Blue
            }

            if (service.imageUrl != null && !service.imageUrl.isEmpty()) {
                if (service.imageUrl.length() > 500) { // Base64
                    ivImage.setImageBitmap(ImageUtil.fromBase64(service.imageUrl));
                } else {
                    // Glide load (simplified, usually context needed)
                    com.bumptech.glide.Glide.with(itemView.getContext())
                            .load(service.imageUrl)
                            .placeholder(R.drawable.ic_admin)
                            .into(ivImage);
                }
            } else {
                ivImage.setImageResource(R.drawable.ic_admin);
            }
        }
    }
}
