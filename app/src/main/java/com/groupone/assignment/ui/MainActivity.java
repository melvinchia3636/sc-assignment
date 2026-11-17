package com.groupone.assignment.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.groupone.assignment.R;
import com.groupone.assignment.databinding.ActivityMainBinding;
import com.groupone.assignment.ui.aqi.AirQualityTab;
import com.groupone.assignment.ui.common.AppIcons;
import com.groupone.assignment.ui.green.GreenSpacesTab;
import com.groupone.assignment.ui.map.StationMapTab;
import com.groupone.assignment.ui.settings.SettingsTab;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;

import org.osmdroid.config.Configuration;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hosts the three tabs and owns permission and location resolution.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG_AIR_QUALITY = "air_quality";
    private static final String TAG_MAP = "station_map";
    private static final String TAG_GREEN_SPACES = "green_spaces";
    private static final String TAG_SETTINGS = "settings";

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
    private int currentTab = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        applySystemBarInsets();
        setUpTabs();
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
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            showScreen(item.getItemId(), true);
            updateNavigationIcons();
            return true;
        });
        showScreen(binding.bottomNavigation.getSelectedItemId(), false);
    }

    private void showScreen(int itemId, boolean animate) {
        String tag = tagFor(itemId);
        Fragment target = getSupportFragmentManager().findFragmentByTag(tag);
        if (target == null) {
            target = createTab(itemId);
        }
        int tab = tabIndex(itemId);
        if (!animate && target.isAdded()) {
            currentTab = tab;
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true);
        if (animate && currentTab >= 0 && tab != currentTab) {
            if (tab > currentTab) {
                transaction.setCustomAnimations(
                        R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                transaction.setCustomAnimations(
                        R.anim.slide_in_left, R.anim.slide_out_right);
            }
        }
        transaction.replace(R.id.fragment_container, target, tag);
        transaction.commit();
        currentTab = tab;
    }

    private String tagFor(int itemId) {
        if (itemId == R.id.tab_map) {
            return TAG_MAP;
        }
        if (itemId == R.id.tab_green) {
            return TAG_GREEN_SPACES;
        }
        if (itemId == R.id.tab_settings) {
            return TAG_SETTINGS;
        }
        return TAG_AIR_QUALITY;
    }

    private Fragment createTab(int itemId) {
        if (itemId == R.id.tab_map) {
            return new StationMapTab();
        }
        if (itemId == R.id.tab_green) {
            return new GreenSpacesTab();
        }
        if (itemId == R.id.tab_settings) {
            return new SettingsTab();
        }
        return new AirQualityTab();
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
