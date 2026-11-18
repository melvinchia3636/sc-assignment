package dev.melvinchia3636.airngreen.services;

import android.content.res.ColorStateList;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import dev.melvinchia3636.airngreen.R;
import dev.melvinchia3636.airngreen.ui.aqi.AirQualityTab;
import dev.melvinchia3636.airngreen.ui.common.AppIcons;
import dev.melvinchia3636.airngreen.ui.green.GreenSpacesTab;
import dev.melvinchia3636.airngreen.ui.map.StationMapTab;
import dev.melvinchia3636.airngreen.ui.settings.SettingsTab;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;

/**
 * Wires the bottom navigation to the navigation graph, including styling,
 * selection state and direction-aware transition animations.
 */
public class TabService {

    private static final class NavTab {
        final int menuId;
        final GoogleMaterial.Icon icon;

        NavTab(int menuId, GoogleMaterial.Icon icon) {
            this.menuId = menuId;
            this.icon = icon;
        }
    }

    /** Lazily loaded so tab classes finish initialising before their icons are read. */
    private static final class NavTabs {
        static final NavTab[] ALL = {
                new NavTab(R.id.tab_aqi, AirQualityTab.NAV_ICON),
                new NavTab(R.id.tab_map, StationMapTab.NAV_ICON),
                new NavTab(R.id.tab_green, GreenSpacesTab.NAV_ICON),
                new NavTab(R.id.tab_settings, SettingsTab.NAV_ICON),
        };
    }

    private final NavController navController;
    private final BottomNavigationView navigation;
    private final View hostView;
    private int pendingDirection;

    private TabService(NavController navController, BottomNavigationView navigation,
                       View hostView) {
        this.navController = navController;
        this.navigation = navigation;
        this.hostView = hostView;
    }

    public static void attach(AppCompatActivity activity, BottomNavigationView navigation) {
        NavHostFragment host = (NavHostFragment) activity.getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (host == null || host.getView() == null) {
            return;
        }
        new TabService(host.getNavController(), navigation, host.getView()).wire();
    }

    private void wire() {
        style();
        navigation.setOnItemSelectedListener(item -> {
            int current = navController.getCurrentDestination() == null
                    ? R.id.tab_aqi : navController.getCurrentDestination().getId();
            if (current != item.getItemId()) {
                navigateTo(item.getItemId(), tabIndex(current), tabIndex(item.getItemId()));
            }
            return true;
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (navigation.getSelectedItemId() != destination.getId()) {
                navigation.getMenu().findItem(destination.getId()).setChecked(true);
            }
            updateIcons();
            playTransition();
        });
    }

    private void navigateTo(int destinationId, int fromTab, int toTab) {
        pendingDirection = toTab > fromTab ? 1 : -1;
        NavOptions options = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(navController.getGraph().getStartDestinationId(), false, true)
                .build();
        navController.navigate(destinationId, null, options);
    }

    /**
     * Navigation Component bakes animations into each back-stack entry, so pop
     * paths replay stale directions. Sliding the host view directly keeps every
     * tab change moving the way the selection moved.
     */
    private void playTransition() {
        if (pendingDirection == 0) {
            return;
        }
        int direction = pendingDirection;
        pendingDirection = 0;

        hostView.post(() -> {
            float width = hostView.getWidth() == 0
                    ? hostView.getResources().getDisplayMetrics().widthPixels
                    : hostView.getWidth();
            hostView.setTranslationX(direction * width * 0.08f);
            hostView.setAlpha(0.7f);
            hostView.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(220L)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        });
    }

    private void style() {
        navigation.setItemTextColor(new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{
                        ContextCompat.getColor(navigation.getContext(), R.color.green_700),
                        ContextCompat.getColor(navigation.getContext(), R.color.muted),
                }));
        updateIcons();
    }

    private void updateIcons() {
        int selected = navigation.getSelectedItemId();
        int active = ContextCompat.getColor(navigation.getContext(), R.color.green_700);
        int inactive = ContextCompat.getColor(navigation.getContext(), R.color.muted);
        for (NavTab tab : NavTabs.ALL) {
            navigation.getMenu().findItem(tab.menuId).setIcon(
                    AppIcons.drawable(navigation.getContext(), tab.icon,
                            selected == tab.menuId ? active : inactive, 24));
        }
    }

    private static int tabIndex(int menuId) {
        for (int index = 0; index < NavTabs.ALL.length; index++) {
            if (NavTabs.ALL[index].menuId == menuId) {
                return index;
            }
        }
        return 0;
    }
}
