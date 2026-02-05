package com.example.localconnect.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.model.Booking;

import java.util.ArrayList;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookings = new ArrayList<>();
    private OnBookingActionListener listener;

    public interface OnBookingActionListener {
        void onCancel(Booking booking);
        void onRate(Booking booking);
    }

    public BookingAdapter(OnBookingActionListener listener) {
        this.listener = listener;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.tvService.setText(booking.workType);
        holder.tvDateTime.setText("Date: " + booking.date);
        holder.tvStatus.setText(booking.status);
        
        // Status Colors
        switch (booking.status) {
            case "PENDING":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FF9800")); // Orange
                holder.btnCancel.setVisibility(View.VISIBLE);
                holder.btnRate.setVisibility(View.GONE);
                break;
            case "ACCEPTED":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
                holder.btnCancel.setVisibility(View.VISIBLE); // Allow cancel before work starts if needed
                holder.btnRate.setVisibility(View.GONE);
                break;
            case "DECLINED":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#F44336")); // Red
                holder.btnCancel.setVisibility(View.GONE);
                holder.btnRate.setVisibility(View.GONE);
                break;
            case "COMPLETED":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#2196F3")); // Blue
                holder.btnCancel.setVisibility(View.GONE);
                holder.btnRate.setVisibility(View.VISIBLE);
                break;
            case "CANCELLED":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#9E9E9E")); // Gray
                holder.btnCancel.setVisibility(View.GONE);
                holder.btnRate.setVisibility(View.GONE);
                break;
        }

        holder.btnCancel.setOnClickListener(v -> listener.onCancel(booking));
        holder.btnRate.setOnClickListener(v -> listener.onRate(booking));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvService, tvDateTime, tvStatus, tvProvider;
        Button btnCancel, btnRate;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvService = itemView.findViewById(R.id.tvBookingService);
            tvDateTime = itemView.findViewById(R.id.tvBookingDateTime);
            tvStatus = itemView.findViewById(R.id.tvBookingStatus);
            tvProvider = itemView.findViewById(R.id.tvBookingProvider);
            btnCancel = itemView.findViewById(R.id.btnCancelBooking);
            btnRate = itemView.findViewById(R.id.btnRateBooking);
        }
    }
}
