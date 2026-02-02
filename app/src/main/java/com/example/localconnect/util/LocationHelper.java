package com.example.localconnect.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationHelper {

    private final FusedLocationProviderClient fusedLocationClient;
    private final Context context;

    public LocationHelper(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public interface LocationResultListener {
        void onLocationFound(String pincode);

        void onError(String error);
    }

    public void getCurrentPincode(Activity activity, LocationResultListener listener) {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            listener.onError("Permission not granted");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            try {
                                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                                        location.getLongitude(), 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    String postalCode = addresses.get(0).getPostalCode();
                                    if (postalCode != null) {
                                        listener.onLocationFound(postalCode);
                                    } else {
                                        listener.onError("Pincode not found in location data");
                                    }
                                } else {
                                    listener.onError("No address found");
                                }
                            } catch (IOException e) {
                                listener.onError("Geocoder error: " + e.getMessage());
                            }
                        } else {
                            listener.onError("Location is null. Turn on GPS.");
                        }
                    }
                });
    }
}
