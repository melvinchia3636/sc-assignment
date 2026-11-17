package com.groupone.assignment.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.groupone.assignment.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Owns runtime permission handling, device location and reverse geocoding.
 *
 * Callers receive either a resolved place or a fallback result together with
 * an optional message to surface.
 */
public class LocationService {

    private static final long LOCATION_TIMEOUT_MS = 12_000;
    private static final double FALLBACK_LATITUDE = 3.1390;
    private static final double FALLBACK_LONGITUDE = 101.6869;
    private static final String FALLBACK_PLACE = "Kuala Lumpur";

    public interface Listener {
        void onLocated(String place, double latitude, double longitude);

        void onFallback(String place, double latitude, double longitude, int messageRes);
    }

    private final Context context;
    private final FusedLocationProviderClient locationClient;
    private final ExecutorService geocoderExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<String> placeName = new MutableLiveData<>(FALLBACK_PLACE);
    private double latitude = FALLBACK_LATITUDE;
    private double longitude = FALLBACK_LONGITUDE;

    private CancellationTokenSource cancellation;
    private Listener listener;

    public LocationService(Context context) {
        this.context = context.getApplicationContext();
        this.locationClient = LocationServices.getFusedLocationProviderClient(this.context);
    }

    public LiveData<String> getPlaceName() {
        return placeName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String placeName() {
        String value = placeName.getValue();
        return value == null ? FALLBACK_PLACE : value;
    }

    public void locate(Listener listener) {
        this.listener = listener;
        requestLocation();
    }

    /** Applies the default location when permission is unavailable. */
    public void applyFallback() {
        setPlace(FALLBACK_PLACE, FALLBACK_LATITUDE, FALLBACK_LONGITUDE);
    }

    public void cancel() {
        if (cancellation != null) {
            cancellation.cancel();
            cancellation = null;
        }
        listener = null;
    }

    public void shutdown() {
        cancel();
        geocoderExecutor.shutdownNow();
    }

    @SuppressLint("MissingPermission")
    private void requestLocation() {
        CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setDurationMillis(LOCATION_TIMEOUT_MS)
                .build();
        cancellation = new CancellationTokenSource();
        locationClient.getCurrentLocation(request, cancellation.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        resolve(location.getLatitude(), location.getLongitude());
                    } else {
                        lastKnownLocation();
                    }
                })
                .addOnFailureListener(failure -> lastKnownLocation());
    }

    @SuppressLint("MissingPermission")
    private void lastKnownLocation() {
        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        resolve(location.getLatitude(), location.getLongitude());
                    } else {
                        fallback(R.string.location_unavailable);
                    }
                })
                .addOnFailureListener(failure -> fallback(R.string.location_unavailable));
    }

    private void resolve(double latitude, double longitude) {
        geocoderExecutor.execute(() -> {
            String place = resolvePlaceName(latitude, longitude);
            mainHandler.post(() -> {
                setPlace(place, latitude, longitude);
                if (listener != null) {
                    listener.onLocated(place, latitude, longitude);
                }
            });
        });
    }

    private void fallback(int messageRes) {
        mainHandler.post(() -> {
            setPlace(FALLBACK_PLACE, FALLBACK_LATITUDE, FALLBACK_LONGITUDE);
            if (listener != null) {
                listener.onFallback(FALLBACK_PLACE, FALLBACK_LATITUDE, FALLBACK_LONGITUDE,
                        messageRes);
            }
        });
    }

    private void setPlace(String place, double latitude, double longitude) {
        placeName.setValue(place);
        this.latitude = latitude;
        this.longitude = longitude;
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
