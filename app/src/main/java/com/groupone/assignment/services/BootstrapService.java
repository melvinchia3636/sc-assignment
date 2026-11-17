package com.groupone.assignment.services;

import android.graphics.Color;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.groupone.assignment.databinding.ActivityMainBinding;

import org.osmdroid.config.Configuration;

/**
 * Handles one-time activity setup: map configuration, edge-to-edge layout,
 * system-bar insets and tab navigation.
 */
public class BootstrapService {

    private final AppCompatActivity activity;

    public BootstrapService(AppCompatActivity activity) {
        this.activity = activity;
    }

    /** Runs before the content view is inflated. */
    public void configure() {
        Configuration.getInstance().load(activity.getApplicationContext(),
                activity.getSharedPreferences("osmdroid", AppCompatActivity.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(activity.getPackageName());
        Configuration.getInstance().setAnimationSpeedShort(180);
        Configuration.getInstance().setAnimationSpeedDefault(250);
        EdgeToEdge.enable(activity,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
    }

    /** Runs after the content view has been inflated. */
    public void attach(ActivityMainBinding binding) {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(binding.root);

        TabService.attach(activity, binding.bottomNavigation);
    }
}
