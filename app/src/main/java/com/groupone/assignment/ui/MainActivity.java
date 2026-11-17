package com.groupone.assignment.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.groupone.assignment.R;
import com.groupone.assignment.databinding.ActivityMainBinding;
import com.groupone.assignment.services.BootstrapService;
import com.groupone.assignment.services.LocationPermissionService;
import com.groupone.assignment.services.LocationService;

/**
 * Hosts the tabs and navigation.
 */
public class MainActivity extends AppCompatActivity {

    private final LocationService.Listener locationListener = new LocationService.Listener() {
        @Override
        public void onLocated(String place, double latitude, double longitude) {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            viewModel.onLocationChanged();
        }

        @Override
        public void onFallback(String place, double latitude, double longitude, int messageRes) {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            Toast.makeText(MainActivity.this, messageRes, Toast.LENGTH_LONG).show();
            viewModel.onLocationChanged();
        }
    };

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private LocationPermissionService permissionService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BootstrapService bootstrap = new BootstrapService(this);
        bootstrap.configure();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        permissionService = new LocationPermissionService(this);

        bootstrap.attach(binding);

        if (savedInstanceState == null) {
            viewModel.startLocationLookup();
            permissionService.ensurePermission(granted -> {
                if (granted) {
                    viewModel.getLocationService().locate(locationListener);
                } else {
                    Toast.makeText(this, R.string.location_permission_denied,
                            Toast.LENGTH_LONG).show();
                    viewModel.getLocationService().applyFallback();
                    viewModel.onLocationChanged();
                }
            });
        }
    }
}
