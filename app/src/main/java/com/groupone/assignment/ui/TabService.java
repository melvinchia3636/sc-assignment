package com.groupone.assignment.ui;

import androidx.navigation.NavController;
import androidx.navigation.NavOptions;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.groupone.assignment.R;
import com.groupone.assignment.ui.common.BaseTab;

/**
 * Wires the bottom navigation to the navigation graph, including the
 * direction-aware tab transition animations.
 */
public class TabService {

    private final NavController navController;
    private final BottomNavigationView navigation;

    public TabService(NavController navController, BottomNavigationView navigation) {
        this.navController = navController;
        this.navigation = navigation;
    }

    public void attach() {
        navigation.setOnItemSelectedListener(item -> {
            int current = navController.getCurrentDestination() == null
                    ? R.id.tab_aqi : navController.getCurrentDestination().getId();
            if (current != item.getItemId()) {
                navigateTo(item.getItemId(),
                        BaseTab.tabIndex(current),
                        BaseTab.tabIndex(item.getItemId()));
            }
            return true;
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (navigation.getSelectedItemId() != destination.getId()) {
                navigation.getMenu().findItem(destination.getId()).setChecked(true);
            }
            BaseTab.updateNavigationIcons(navigation);
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
}
