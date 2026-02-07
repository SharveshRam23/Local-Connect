package com.example.localconnect.worker;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.Issue;
import com.example.localconnect.model.Notice;
import com.example.localconnect.model.ServiceProvider;
import com.example.localconnect.util.GeofenceHelper;
import com.google.android.gms.location.Geofence;
import java.util.ArrayList;
import java.util.List;

public class GeofenceRegistrationWorker extends Worker {
    public GeofenceRegistrationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("local_connect_prefs", Context.MODE_PRIVATE);
        String pincode = prefs.getString("user_pincode", null);

        if (pincode == null) return Result.success();

        AppDatabase db = AppDatabase.getDatabase(context);
        GeofenceHelper helper = new GeofenceHelper(context);

        List<Geofence> geofences = new ArrayList<>();

        // Fetch nearby Providers
        List<ServiceProvider> providers = db.providerDao().getProvidersByPincode(pincode);
        if (providers != null) {
            for (ServiceProvider p : providers) {
                if (p.latitude != null && p.longitude != null && p.isAvailable) {
                    geofences.add(helper.createGeofence("PROVIDER_" + p.id, p.latitude, p.longitude, 200));
                }
            }
        }

        // Fetch nearby Issues
        List<Issue> issues = db.issueDao().getIssuesByPincode(pincode);
        if (issues != null) {
            for (Issue i : issues) {
                if (i.latitude != null && i.longitude != null && "PENDING".equals(i.status)) {
                    geofences.add(helper.createGeofence("ISSUE_" + i.id, i.latitude, i.longitude, 150));
                }
            }
        }

        // Fetch nearby Notices
        List<Notice> notices = db.noticeDao().getNoticesForUser(pincode);
        if (notices != null) {
            for (Notice n : notices) {
                if (n.latitude != null && n.longitude != null && n.isGeofenceEnabled) {
                    geofences.add(helper.createGeofence("NOTICE_" + n.id, n.latitude, n.longitude, 300));
                }
            }
        }

        // Limit to 50 geofences (Google limit is around 100 per app)
        if (geofences.size() > 50) {
            geofences = geofences.subList(0, 50);
        }

        if (!geofences.isEmpty()) {
            helper.removeGeofences(); // Clear old ones
            helper.addGeofences(geofences);
        }

        return Result.success();
    }
}
