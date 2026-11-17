package com.groupone.assignment.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.groupone.assignment.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Resolves the device location and a human-readable place name.
 *
 * Callers remain responsible for runtime permission handling; this class only
 * performs the location request and geocoding.
 */
public class LocationService {

    private static final long LOCATION_TIMEOUT_MS = 12_000;

    public interface Listener {
        void onLocated(String place, double latitude, double longitude);

        void onUnavailable();
    }

    private final Context context;
    private final FusedLocationProviderClient locationClient;
    private final ExecutorService geocoderExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private CancellationTokenSource cancellation;

    public LocationService(Context context) {
        this.context = context.getApplicationContext();
        this.locationClient = LocationServices.getFusedLocationProviderClient(this.context);
    }

    @SuppressLint("MissingPermission")
    public void request(Listener listener) {
        cancel();
        CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setDurationMillis(LOCATION_TIMEOUT_MS)
                .build();
        cancellation = new CancellationTokenSource();
        locationClient.getCurrentLocation(request, cancellation.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        resolve(listener, location.getLatitude(), location.getLongitude());
                    } else {
                        lastKnown(listener);
                    }
                })
                .addOnFailureListener(failure -> lastKnown(listener));
    }

    public void cancel() {
        if (cancellation != null) {
            cancellation.cancel();
            cancellation = null;
        }
    }

    public void shutdown() {
        cancel();
        geocoderExecutor.shutdownNow();
    }

    @SuppressLint("MissingPermission")
    private void lastKnown(Listener listener) {
        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        resolve(listener, location.getLatitude(), location.getLongitude());
                    } else {
                        unavailable(listener);
                    }
                })
                .addOnFailureListener(failure -> unavailable(listener));
    }

    private void resolve(Listener listener, double latitude, double longitude) {
        geocoderExecutor.execute(() -> {
            String place = resolvePlaceName(latitude, longitude);
            mainHandler.post(() -> listener.onLocated(place, latitude, longitude));
        });
    }

    private void unavailable(Listener listener) {
        mainHandler.post(listener::onUnavailable);
    }

    private String resolvePlaceName(double latitude, double longitude) {
        try {
            List<Address> addresses = new Geocoder(context, Locale.getDefault())
                    .getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String locality = address.getLocality();
                if (locality == null || locality.trim().isEmpty()) {
                    locality = address.getSubAdminArea();
                }
                if (locality == null || locality.trim().isEmpty()) {
                    locality = address.getAdminArea();
                }
                if (locality != null && !locality.trim().isEmpty()) {
                    return locality;
                }
            }
        } catch (IOException | IllegalArgumentException unavailable) {
            // Fall through to a generic label.
        }
        return context.getString(R.string.your_location);
    }
}
