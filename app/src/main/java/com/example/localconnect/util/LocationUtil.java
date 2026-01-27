package com.example.localconnect.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;

import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Locale;

public class LocationUtil {

    public interface LocationCallback {
        void onResult(String pincode);
    }

    public static void getPincode(Context context, LocationCallback callback) {

        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(context);

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            callback.onResult(null);
            return;
        }

        client.getLastLocation().addOnSuccessListener(location -> {

            if (location != null) {
                try {

                    Geocoder geocoder =
                            new Geocoder(context, Locale.getDefault());

                    List<Address> list =
                            geocoder.getFromLocation(
                                    location.getLatitude(),
                                    location.getLongitude(),
                                    1
                            );

                    if (list != null && !list.isEmpty()) {
                        callback.onResult(list.get(0).getPostalCode());
                    } else {
                        callback.onResult(null);
                    }

                } catch (Exception e) {
                    callback.onResult(null);
                }

            } else {
                callback.onResult(null);
            }
        });
    }
}
