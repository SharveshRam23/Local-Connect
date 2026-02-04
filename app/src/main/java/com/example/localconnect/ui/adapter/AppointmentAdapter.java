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
import com.example.localconnect.model.Appointment;

import java.util.ArrayList;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private List<Appointment> appointments = new ArrayList<>();
    private OnAppointmentActionListener listener;

    public interface OnAppointmentActionListener {
        void onAccept(Appointment appointment);

        void onReject(Appointment appointment);

        void onCall(String phone);
    }

    public void setAppointments(List<Appointment> appointments, OnAppointmentActionListener listener) {
        this.appointments = appointments;
        this.listener = listener;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.tvUser.setText(appointment.userName);
        holder.tvIssue.setText("Issue: " + appointment.issue);
        holder.tvDate.setText("Date: " + appointment.date);
        holder.tvTime.setText("Time: " + appointment.time);
        holder.tvPhone.setText("Phone: " + appointment.userPhone);
        holder.tvStatus.setText("Status: " + appointment.status);

        if ("PENDING".equals(appointment.status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#FFA500")); // Orange
            holder.layoutActions.setVisibility(View.VISIBLE);
        } else if ("CONFIRMED".equals(appointment.status)) {
            holder.tvStatus.setTextColor(Color.GREEN);
            holder.layoutActions.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setTextColor(Color.RED);
            holder.layoutActions.setVisibility(View.GONE);
        }

        holder.btnAccept.setOnClickListener(v -> listener.onAccept(appointment));
        holder.btnReject.setOnClickListener(v -> listener.onReject(appointment));
        holder.btnCall.setOnClickListener(v -> listener.onCall(appointment.userPhone));
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvIssue, tvDate, tvTime, tvPhone, tvStatus;
        Button btnAccept, btnReject, btnCall;
        LinearLayout layoutActions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tvAppointmentUser);
            tvIssue = itemView.findViewById(R.id.tvAppointmentIssue);
            tvDate = itemView.findViewById(R.id.tvAppointmentDate);
            tvTime = itemView.findViewById(R.id.tvAppointmentTime);
            tvPhone = itemView.findViewById(R.id.tvAppointmentPhone);
            tvStatus = itemView.findViewById(R.id.tvAppointmentStatus);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnCall = itemView.findViewById(R.id.btnCallUser);
            layoutActions = itemView.findViewById(R.id.layoutActions);
        }
    }
}
