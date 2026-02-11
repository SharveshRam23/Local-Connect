package com.example.localconnect.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.localconnect.model.MandatoryService;
import com.example.localconnect.receiver.GeofenceReceiver;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Collections;
import java.util.List;

public class GeofenceHelper {

    private static final String TAG = "GeofenceHelper";
    private static final float GEOFENCE_RADIUS_IN_METERS = 300; // 300m radius
    private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = Geofence.NEVER_EXPIRE;

    private final Context context;
    private final GeofencingClient geofencingClient;

    public GeofenceHelper(Context context) {
        this.context = context;
        this.geofencingClient = LocationServices.getGeofencingClient(context);
    }

    public Geofence createGeofence(String id, double lat, double lng, float radius) {
        return new Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(lat, lng, radius)
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setNotificationResponsiveness(1000 * 60 * 5) // 5 minutes responsiveness
                .build();
    }

    public GeofencingRequest getGeofencingRequest(List<Geofence> geofences) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofences)
                .build();
    }

    public void addGeofences(List<Geofence> geofences) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted for geofencing");
            return;
        }

        if (geofences.isEmpty()) return;

        geofencingClient.addGeofences(getGeofencingRequest(geofences), getGeofencePendingIntent())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Geofences added"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to add geofences: " + e.getMessage()));
    }

    public void addGeofencesForServices(List<MandatoryService> services) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted for geofencing");
            return;
        }

        for (MandatoryService service : services) {
            // Validate coordinates: latitude must be -90 to 90, longitude must be -180 to 180
            if (service.latitude != 0.0 && service.longitude != 0.0 &&
                isValidLatitude(service.latitude) && isValidLongitude(service.longitude)) {
                addGeofence(service);
            } else if (service.latitude != 0.0 || service.longitude != 0.0) {
                Log.w(TAG, "Skipping geofence for service '" + service.name + "' due to invalid coordinates: lat=" + service.latitude + ", lng=" + service.longitude);
            }
        }
    }

    private boolean isValidLatitude(double lat) {
        return lat >= -90.0 && lat <= 90.0;
    }

    private boolean isValidLongitude(double lng) {
        return lng >= -180.0 && lng <= 180.0;
    }

    private void addGeofence(MandatoryService service) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Geofence geofence = new Geofence.Builder()
                .setRequestId(service.id + "|" + service.name) // Encode ID and Name
                .setCircularRegion(service.latitude, service.longitude, GEOFENCE_RADIUS_IN_METERS)
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        GeofencingRequest request = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

        geofencingClient.addGeofences(request, getGeofencePendingIntent())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Geofence added for: " + service.name))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to add geofence: " + e.getMessage()));
    }

    public void removeGeofences() {
        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Geofences removed"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to remove geofences: " + e.getMessage()));
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(context, GeofenceReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addGeofences() and removeGeofences().
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
    }
}
