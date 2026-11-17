package com.groupone.assignment.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.groupone.assignment.R;
import com.groupone.assignment.databinding.ActivityMainBinding;
import com.groupone.assignment.ui.common.AppIcons;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;

import org.osmdroid.config.Configuration;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hosts the tabs and owns permission and location resolution.
 */
public class MainActivity extends AppCompatActivity {

    private static final double DEFAULT_LATITUDE = 3.1390;
    private static final double DEFAULT_LONGITUDE = 101.6869;
    private static final String DEFAULT_PLACE = "Kuala Lumpur";

    private static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    this::onPermissionResult);

    private final ExecutorService geocoderExecutor = Executors.newSingleThreadExecutor();

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private CancellationTokenSource locationCancellation;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().setAnimationSpeedShort(180);
        Configuration.getInstance().setAnimationSpeedDefault(250);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        applySystemBarInsets();
        setUpTabs();
        styleNavigation();
        updateNavigationIcons();

        if (savedInstanceState == null) {
            if (hasLocationPermission()) {
                loadFromDeviceLocation();
            } else {
                permissionLauncher.launch(LOCATION_PERMISSIONS);
            }
        }
    }

    private void setUpTabs() {
        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (host == null) {
            return;
        }
        navController = host.getNavController();

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int current = navController.getCurrentDestination() == null
                    ? R.id.tab_aqi : navController.getCurrentDestination().getId();
            if (current != item.getItemId()) {
                navigateTo(item.getItemId(), tabIndex(current), tabIndex(item.getItemId()));
            }
            return true;
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (binding.bottomNavigation.getSelectedItemId() != destination.getId()) {
                binding.bottomNavigation.getMenu().findItem(destination.getId()).setChecked(true);
            }
            updateNavigationIcons();
        });
    }

    private void navigateTo(int destinationId, int fromTab, int toTab) {
        boolean forward = toTab > fromTab;
        NavOptions options = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(navController.getGraph().getStartDestinationId(), false, true)
                .setEnterAnim(forward ? R.anim.slide_in_right : R.anim.slide_in_left)
                .setExitAnim(forward ? R.anim.slide_out_left : R.anim.slide_out_right)
                .setPopEnterAnim(forward ? R.anim.slide_in_left : R.anim.slide_in_right)
                .setPopExitAnim(forward ? R.anim.slide_out_right : R.anim.slide_out_left)
                .build();
        navController.navigate(destinationId, null, options);
    }

    private int tabIndex(int itemId) {
        if (itemId == R.id.tab_map) {
            return 1;
        }
        if (itemId == R.id.tab_green) {
            return 2;
        }
        if (itemId == R.id.tab_settings) {
            return 3;
        }
        return 0;
    }

    private void onPermissionResult(Map<String, Boolean> result) {
        boolean granted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION))
                || Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
        if (granted) {
            loadFromDeviceLocation();
        } else {
            Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_LONG).show();
            useFallbackLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void loadFromDeviceLocation() {
        if (!hasLocationPermission()) {
            useFallbackLocation();
            return;
        }
        viewModel.startLocationLookup();
        CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setDurationMillis(12_000)
                .build();
        locationCancellation = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(request, locationCancellation.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        onLocated(location.getLatitude(), location.getLongitude());
                    } else {
                        loadLastKnownLocation();
                    }
                })
                .addOnFailureListener(this, failure -> loadLastKnownLocation());
    }

    @SuppressLint("MissingPermission")
    private void loadLastKnownLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        onLocated(location.getLatitude(), location.getLongitude());
                    } else {
                        onLocationUnavailable();
                    }
                })
                .addOnFailureListener(this, failure -> onLocationUnavailable());
    }

    private void onLocated(double latitude, double longitude) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        geocoderExecutor.execute(() -> {
            String name = resolvePlaceName(latitude, longitude);
            runOnUiThread(() -> viewModel.setLocation(name, latitude, longitude));
        });
    }

    private void onLocationUnavailable() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        Toast.makeText(this, R.string.location_unavailable, Toast.LENGTH_LONG).show();
        useFallbackLocation();
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void useFallbackLocation() {
        viewModel.setLocation(DEFAULT_PLACE, DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
    }

    private String resolvePlaceName(double latitude, double longitude) {
        try {
            List<Address> addresses = new Geocoder(this, Locale.getDefault())
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
        return getString(R.string.your_location);
    }

    private void applySystemBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(binding.root);
    }

    private void styleNavigation() {
        binding.bottomNavigation.setItemTextColor(new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{
                        ContextCompat.getColor(this, R.color.green_700),
                        ContextCompat.getColor(this, R.color.muted),
                }));
    }

    private void updateNavigationIcons() {
        BottomNavigationView navigation = binding.bottomNavigation;
        int selected = navigation.getSelectedItemId();
        int active = Color.parseColor("#1B5E20");
        int inactive = Color.parseColor("#5F665F");
        navigation.getMenu().findItem(R.id.tab_aqi).setIcon(
                AppIcons.drawable(this, GoogleMaterial.Icon.gmd_cloud,
                        selected == R.id.tab_aqi ? active : inactive, 24));
        navigation.getMenu().findItem(R.id.tab_map).setIcon(
                AppIcons.drawable(this, GoogleMaterial.Icon.gmd_map,
                        selected == R.id.tab_map ? active : inactive, 24));
        navigation.getMenu().findItem(R.id.tab_green).setIcon(
                AppIcons.drawable(this, GoogleMaterial.Icon.gmd_park,
                        selected == R.id.tab_green ? active : inactive, 24));
        navigation.getMenu().findItem(R.id.tab_settings).setIcon(
                AppIcons.drawable(this, GoogleMaterial.Icon.gmd_settings,
                        selected == R.id.tab_settings ? active : inactive, 24));
    }

    @Override
    protected void onDestroy() {
        if (locationCancellation != null) {
            locationCancellation.cancel();
        }
        geocoderExecutor.shutdownNow();
        super.onDestroy();
    }
}
