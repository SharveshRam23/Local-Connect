package com.example.localconnect.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.model.Booking;

import java.util.ArrayList;
import java.util.List;

public class ProviderBookingAdapter extends RecyclerView.Adapter<ProviderBookingAdapter.ViewHolder> {

    private List<Booking> bookings = new ArrayList<>();
    private OnActionClickListener listener;

    public interface OnActionClickListener {
        void onApprove(Booking booking);
        void onDecline(Booking booking);
        void onComplete(Booking booking);
    }

    public ProviderBookingAdapter(OnActionClickListener listener) {
        this.listener = listener;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_provider_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.tvService.setText(booking.workType);
        holder.tvUser.setText("User ID: " + booking.userId); // In a real app, you'd fetch user name
        holder.tvDateTime.setText("Date: " + booking.date);
        holder.tvStatus.setText(booking.status);
        
        holder.tvAddress.setText(booking.address != null && !booking.address.isEmpty() ? "Address: " + booking.address : "Address: Not provided");
        if (booking.latitude != 0.0) {
            holder.tvLocation.setText(String.format("Location: %.4f, %.4f", booking.latitude, booking.longitude));
            holder.tvLocation.setVisibility(View.VISIBLE);
        } else {
            holder.tvLocation.setVisibility(View.GONE);
        }
        
        if (booking.status.equals("PENDING")) {
            holder.layoutActions.setVisibility(View.VISIBLE);
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnDecline.setVisibility(View.VISIBLE);
            holder.btnComplete.setVisibility(View.GONE);
            holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));
        } else if (booking.status.equals("ACCEPTED")) {
            holder.layoutActions.setVisibility(View.VISIBLE);
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnDecline.setVisibility(View.GONE);
            holder.btnComplete.setVisibility(View.VISIBLE);
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.layoutActions.setVisibility(View.GONE);
            if (booking.status.equals("DECLINED")) holder.tvStatus.setTextColor(Color.parseColor("#F44336"));
            else if (booking.status.equals("COMPLETED")) holder.tvStatus.setTextColor(Color.parseColor("#2196F3"));
            else holder.tvStatus.setTextColor(Color.GRAY);
        }

        holder.btnApprove.setOnClickListener(v -> listener.onApprove(booking));
        holder.btnDecline.setOnClickListener(v -> listener.onDecline(booking));
        holder.btnComplete.setOnClickListener(v -> listener.onComplete(booking));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvService, tvUser, tvDateTime, tvStatus, tvAddress, tvLocation;
        LinearLayout layoutActions;
        Button btnApprove, btnDecline, btnComplete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvService = itemView.findViewById(R.id.tvRequestService);
            tvUser = itemView.findViewById(R.id.tvRequestUser);
            tvDateTime = itemView.findViewById(R.id.tvRequestDateTime);
            tvStatus = itemView.findViewById(R.id.tvRequestStatus);
            tvAddress = itemView.findViewById(R.id.tvRequestAddress);
            tvLocation = itemView.findViewById(R.id.tvRequestLocation);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnDecline = itemView.findViewById(R.id.btnDecline);
            btnComplete = itemView.findViewById(R.id.btnComplete);
        }
    }
}
