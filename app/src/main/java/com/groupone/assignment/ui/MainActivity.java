package com.groupone.assignment.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.groupone.assignment.R;
import com.groupone.assignment.databinding.ActivityMainBinding;
import com.groupone.assignment.services.LocationService;
import com.groupone.assignment.services.TabService;
import com.groupone.assignment.ui.common.BaseTab;

import org.osmdroid.config.Configuration;

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
            viewModel.setLocation(place, latitude, longitude);
        }

        @Override
        public void onFallback(String place, double latitude, double longitude, int messageRes) {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            Toast.makeText(MainActivity.this, messageRes, Toast.LENGTH_LONG).show();
            viewModel.setLocation(place, latitude, longitude);
        }
    };

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
            viewModel.startLocationLookup();
            locationService.locate(locationListener);
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
