package com.groupone.assignment.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import androidx.navigation.fragment.NavHostFragment;

import com.groupone.assignment.R;
import com.groupone.assignment.databinding.ActivityMainBinding;
import com.groupone.assignment.location.LocationService;
import com.groupone.assignment.ui.common.BaseTab;

import org.osmdroid.config.Configuration;

import java.util.Map;

/**
 * Hosts the tabs, permission handling and navigation.
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

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private LocationService locationService;

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
        locationService = new LocationService(this);

        applySystemBarInsets();
        setUpTabs();
        BaseTab.styleNavigation(binding.bottomNavigation);

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
        new TabService(host.getNavController(), binding.bottomNavigation).attach();
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
        locationService.request(new LocationService.Listener() {
            @Override
            public void onLocated(String place, double latitude, double longitude) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                viewModel.setLocation(place, latitude, longitude);
            }

            @Override
            public void onUnavailable() {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                Toast.makeText(MainActivity.this, R.string.location_unavailable,
                        Toast.LENGTH_LONG).show();
                useFallbackLocation();
            }
        });
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void useFallbackLocation() {
        viewModel.setLocation(DEFAULT_PLACE, DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
    }

    private void applySystemBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(binding.root);
    }

    @Override
    protected void onDestroy() {
        locationService.shutdown();
        super.onDestroy();
    }
}
