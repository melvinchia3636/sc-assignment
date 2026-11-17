package com.groupone.assignment.ui.common;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.groupone.assignment.R;
import com.groupone.assignment.repositories.model.Station;
import com.groupone.assignment.repositories.model.StationDetail;
import com.groupone.assignment.ui.MainViewModel;
import com.groupone.assignment.ui.aqi.AirQualityTab;
import com.groupone.assignment.ui.green.GreenSpacesTab;
import com.groupone.assignment.ui.map.StationMapTab;
import com.groupone.assignment.ui.settings.SettingsTab;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;

import java.util.List;

/**
 * Base for bottom-bar tabs: owns the ViewBinding lifecycle, navigation icon
 * styling and the shared {@link MainViewModel} observers.
 */
public abstract class BaseTab<VB extends ViewBinding> extends Fragment {

    private static final class NavTab {
        final int menuId;
        final GoogleMaterial.Icon icon;

        NavTab(int menuId, GoogleMaterial.Icon icon) {
            this.menuId = menuId;
            this.icon = icon;
        }
    }

    private static final NavTab[] NAV_TABS = {
            new NavTab(R.id.tab_aqi, AirQualityTab.NAV_ICON),
            new NavTab(R.id.tab_map, StationMapTab.NAV_ICON),
            new NavTab(R.id.tab_green, GreenSpacesTab.NAV_ICON),
            new NavTab(R.id.tab_settings, SettingsTab.NAV_ICON),
    };

    protected VB binding;

    private MainViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = createBinding(inflater, container);
        return binding.getRoot();
    }

    protected abstract VB createBinding(LayoutInflater inflater,
                                        @Nullable ViewGroup container);

    protected MainViewModel sharedViewModel() {
        if (viewModel == null) {
            viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        }
        return viewModel;
    }

    protected void observeStation(Observer<StationDetail> observer) {
        sharedViewModel().getStation().observe(getViewLifecycleOwner(), observer);
    }

    protected void observeStations(Observer<List<Station>> observer) {
        sharedViewModel().getStations().observe(getViewLifecycleOwner(), observer);
    }

    protected void observeStationsLoading(Observer<Boolean> observer) {
        sharedViewModel().getStationsLoading().observe(getViewLifecycleOwner(), observer);
    }

    public static void styleNavigation(BottomNavigationView navigation) {
        navigation.setItemTextColor(new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{
                        ContextCompat.getColor(navigation.getContext(), R.color.green_700),
                        ContextCompat.getColor(navigation.getContext(), R.color.muted),
                }));
        updateNavigationIcons(navigation);
    }

    public static void updateNavigationIcons(BottomNavigationView navigation) {
        int selected = navigation.getSelectedItemId();
        int active = Color.parseColor("#1B5E20");
        int inactive = Color.parseColor("#5F665F");
        for (NavTab tab : NAV_TABS) {
            navigation.getMenu().findItem(tab.menuId).setIcon(
                    AppIcons.drawable(navigation.getContext(), tab.icon,
                            selected == tab.menuId ? active : inactive, 24));
        }
    }

    public static int tabIndex(int menuId) {
        for (int index = 0; index < NAV_TABS.length; index++) {
            if (NAV_TABS[index].menuId == menuId) {
                return index;
            }
        }
        return 0;
    }

    protected void observeError(Observer<String> observer) {
        sharedViewModel().getError().observe(getViewLifecycleOwner(), observer);
    }

    protected void observeLoading(SwipeRefreshLayout swipeRefresh) {
        sharedViewModel().getLoading().observe(getViewLifecycleOwner(),
                loading -> swipeRefresh.setRefreshing(Boolean.TRUE.equals(loading)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
