package com.example.localconnect.ui.provider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.Appointment;
import com.example.localconnect.model.ServiceProvider; // Added import
import com.example.localconnect.ui.adapter.AppointmentAdapter;

import java.util.List;

public class ProviderDashboardActivity extends AppCompatActivity {

    private Switch switchAvailability;
    private RecyclerView rvAppointments;
    private AppointmentAdapter adapter;
    private int providerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_dashboard);

        com.example.localconnect.util.SessionManager sessionManager = new com.example.localconnect.util.SessionManager(
                this);
        providerId = sessionManager.getProviderId();
        String providerName = sessionManager.getProviderName();

        TextView tvTitle = findViewById(R.id.tvDashboardTitle);
        tvTitle.setText("Welcome, " + providerName);

        switchAvailability = findViewById(R.id.switchAvailability);
        rvAppointments = findViewById(R.id.rvProviderAppointments);
        Button btnLogout = findViewById(R.id.btnLogoutProvider);

        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter();
        rvAppointments.setAdapter(adapter);

        btnLogout.setOnClickListener(v -> logout());

        loadProviderStatus(); // Load availability status

        switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> updateAvailability(isChecked));

        loadAppointments();
    }

    private void loadProviderStatus() {
        if (providerId == -1)
            return;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            ServiceProvider provider = AppDatabase.getDatabase(getApplicationContext()).providerDao()
                    .getProviderById(providerId);
            runOnUiThread(() -> {
                if (provider != null) {
                    // Avoid triggering listener during setup
                    switchAvailability.setOnCheckedChangeListener(null);
                    switchAvailability.setChecked(provider.isAvailable);
                    switchAvailability
                            .setOnCheckedChangeListener((buttonView, isChecked) -> updateAvailability(isChecked));
                }
            });
        });
    }

    private void updateAvailability(boolean isAvailable) {
        if (providerId == -1)
            return;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getDatabase(getApplicationContext()).providerDao().updateAvailability(providerId, isAvailable);
            runOnUiThread(() -> Toast.makeText(this, "Availability Updated", Toast.LENGTH_SHORT).show());
        });
    }

    private void loadAppointments() {
        if (providerId == -1)
            return;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Appointment> appointments = AppDatabase.getDatabase(getApplicationContext()).appointmentDao()
                    .getAppointmentsForProvider(providerId);
            runOnUiThread(() -> {
                if (appointments != null) {
                    adapter.setAppointments(appointments, new AppointmentAdapter.OnAppointmentActionListener() {
                        @Override
                        public void onAccept(Appointment appointment) {
                            updateAppointmentStatus(appointment, "CONFIRMED");
                        }

                        @Override
                        public void onReject(Appointment appointment) {
                            updateAppointmentStatus(appointment, "REJECTED");
                        }

                        @Override
                        public void onCall(String phone) {
                            Intent intent = new Intent(Intent.ACTION_DIAL); // Use ACTION_DIAL to avoid immediate
                                                                            // permission crash if not granted, or
                                                                            // ACTION_CALL if checked
                            intent.setData(Uri.parse("tel:" + phone));
                            startActivity(intent);
                        }
                    });
                }
            });
        });
    }

    private void updateAppointmentStatus(Appointment appointment, String status) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getDatabase(getApplicationContext()).appointmentDao().updateStatus(appointment.id, status);
            runOnUiThread(() -> {
                Toast.makeText(this, "Status Updated to " + status, Toast.LENGTH_SHORT).show();
                loadAppointments(); // Reload to refresh UI
            });
        });
    }

    private void logout() {
        new com.example.localconnect.util.SessionManager(this).logout();
        Intent intent = new Intent(this, ProviderLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
