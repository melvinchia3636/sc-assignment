package com.groupone.assignment.ui;

import android.content.res.ColorStateList;
import android.graphics.Color;

import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.groupone.assignment.R;
import com.groupone.assignment.ui.aqi.AirQualityTab;
import com.groupone.assignment.ui.common.AppIcons;
import com.groupone.assignment.ui.green.GreenSpacesTab;
import com.groupone.assignment.ui.map.StationMapTab;
import com.groupone.assignment.ui.settings.SettingsTab;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;

/**
 * Owns the bottom-navigation styling for the app's tabs.
 */
public final class TabNavigation {

    private static final class Tab {
        final int menuId;
        final GoogleMaterial.Icon icon;

        Tab(int menuId, GoogleMaterial.Icon icon) {
            this.menuId = menuId;
            this.icon = icon;
        }
    }

    private static final Tab[] TABS = {
            new Tab(R.id.tab_aqi, AirQualityTab.NAV_ICON),
            new Tab(R.id.tab_map, StationMapTab.NAV_ICON),
            new Tab(R.id.tab_green, GreenSpacesTab.NAV_ICON),
            new Tab(R.id.tab_settings, SettingsTab.NAV_ICON),
    };

    private TabNavigation() {
    }

    public static void style(BottomNavigationView navigation) {
        navigation.setItemTextColor(new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{
                        ContextCompat.getColor(navigation.getContext(), R.color.green_700),
                        ContextCompat.getColor(navigation.getContext(), R.color.muted),
                }));
        updateIcons(navigation);
    }

    public static void updateIcons(BottomNavigationView navigation) {
        int selected = navigation.getSelectedItemId();
        int active = Color.parseColor("#1B5E20");
        int inactive = Color.parseColor("#5F665F");
        for (Tab tab : TABS) {
            navigation.getMenu().findItem(tab.menuId).setIcon(
                    AppIcons.drawable(navigation.getContext(), tab.icon,
                            selected == tab.menuId ? active : inactive, 24));
        }
    }

    public static int indexOf(int menuId) {
        for (int index = 0; index < TABS.length; index++) {
            if (TABS[index].menuId == menuId) {
                return index;
            }
        }
        return 0;
    }
}
