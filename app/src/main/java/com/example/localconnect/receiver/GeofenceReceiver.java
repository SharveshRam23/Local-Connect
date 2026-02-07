package com.example.localconnect.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.example.localconnect.util.NotificationUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import java.util.List;

public class GeofenceReceiver extends BroadcastReceiver {
    private static final String TAG = "GeofenceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "GeofencingEvent error: " + geofencingEvent.getErrorCode());
            return;
        }

        int transitionType = geofencingEvent.getGeofenceTransition();

        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            if (triggeringGeofences != null) {
                for (Geofence geofence : triggeringGeofences) {
                    processGeofence(context, geofence);
                }
            }
        }
    }

    private void processGeofence(Context context, Geofence geofence) {
        String requestId = geofence.getRequestId();
        Log.d(TAG, "Geofence entered: " + requestId);

        String title = "Nearby Smart Alert";
        String message = "You are near a relevant community point.";

        if (requestId.startsWith("PROVIDER_")) {
            title = "Provider Nearby";
            message = "A service provider is available in this area.";
        } else if (requestId.startsWith("ISSUE_")) {
            title = "Reported Issue Area";
            message = "There is a reported issue in this location.";
        } else if (requestId.startsWith("NOTICE_")) {
            title = "Local Event/Notice";
            message = "Check out this community alert for this zone.";
        } else if (requestId.contains("|")) {
            // Mandatory Service: ID|Name
            String[] parts = requestId.split("\\|");
            if (parts.length > 1) {
                title = "Essential Service Nearby";
                message = "You are near " + parts[1];
            }
        }

        NotificationUtil.showGeofenceNotification(context, title, message);
    }
}
