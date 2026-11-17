package dev.melvinchia3636.airngreen.services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import java.util.Map;

/**
 * Requests location permission from an Activity.
 */
public class LocationPermissionService {

    public interface Callback {
        void onResult(boolean granted);
    }

    private static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private final Context context;
    private final ActivityResultLauncher<String[]> launcher;

    private Callback pending;

    public LocationPermissionService(ComponentActivity activity) {
        this.context = activity.getApplicationContext();
        this.launcher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::deliver);
    }

    public void ensurePermission(Callback callback) {
        if (hasPermission()) {
            callback.onResult(true);
            return;
        }
        pending = callback;
        launcher.launch(LOCATION_PERMISSIONS);
    }

    private boolean hasPermission() {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void deliver(Map<String, Boolean> result) {
        boolean granted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION))
                || Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
        if (pending != null) {
            pending.onResult(granted);
            pending = null;
        }
    }
}
