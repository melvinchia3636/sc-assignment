package com.groupone.assignment.ui.aqi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.groupone.assignment.repositories.model.StationDetail;
import com.groupone.assignment.databinding.TabAqiBinding;
import com.groupone.assignment.services.AudienceService;
import com.groupone.assignment.ui.MainViewModel;
import com.groupone.assignment.ui.common.BaseTab;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;

/**
 * Air-quality screen: reading, pollutants, weather and trends.
 */
public class AirQualityTab extends BaseTab<TabAqiBinding> {

    public static final GoogleMaterial.Icon NAV_ICON = GoogleMaterial.Icon.gmd_cloud;

    private MainViewModel viewModel;

    @Override
    protected TabAqiBinding createBinding(LayoutInflater inflater,
            ViewGroup container) {
        return TabAqiBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = sharedViewModel();

        binding.swipeRefresh.setOnRefreshListener(viewModel::refresh);
        binding.content.setOnRetryListener(viewModel::refresh);

        observeAirQuality();
    }

    private void observeAirQuality() {
        viewModel.airQuality().getStation().observe(getViewLifecycleOwner(), this::renderStation);
        observeError(message -> {
            if (message != null) {
                renderError(message);
            }
        });

        viewModel.getLocationService().getPlaceName().observe(getViewLifecycleOwner(),
                name -> binding.content.setPlaceName(name));
        observeLoading(binding.swipeRefresh);
    }

    private void renderStation(StationDetail station) {
        binding.content.bind(station, AudienceService.INSTANCE.current(requireContext()));
        binding.swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) {
            binding.content.bindAudience(AudienceService.INSTANCE.current(requireContext()));
        }
    }

    private void renderError(String message) {
        binding.content.showError(message);
        binding.swipeRefresh.setRefreshing(false);
    }
}
