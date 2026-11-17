package com.groupone.assignment.ui.aqi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.groupone.assignment.R;
import com.groupone.assignment.repositories.model.StationDetail;
import com.groupone.assignment.databinding.TabAqiBinding;
import com.groupone.assignment.services.AudienceService;
import com.groupone.assignment.ui.MainViewModel;
import com.groupone.assignment.ui.common.BaseTab;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Air-quality screen: reading, pollutants, weather and trends.
 */
public class AirQualityTab extends BaseTab<TabAqiBinding> {

    public static final GoogleMaterial.Icon NAV_ICON = GoogleMaterial.Icon.gmd_cloud;

    private MainViewModel viewModel;
    private double lastAqi = Double.NaN;

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
        binding.errorCard.setOnRetryListener(viewModel::refresh);
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.airQuality().getStation().observe(getViewLifecycleOwner(), this::render);
        observeError(message -> {
            if (message != null) {
                showError(message);
            }
        });
        viewModel.getLocationService().getPlaceName().observe(getViewLifecycleOwner(),
                name -> binding.cityTitle.setText(name));
        observeLoading(binding.swipeRefresh);
    }

    private void render(StationDetail station) {
        lastAqi = station.getAqi();
        binding.aqiHero.bind(station, viewModel.getLocationService().placeName());

        binding.guidanceSection.bind(
                AudienceService.INSTANCE.current(requireContext()), station.getAqi());

        long updatedAt = station.getUpdatedAt();
        binding.updatedText.setText(updatedAt > 0
                ? getString(R.string.updated_at, formatEpoch(updatedAt))
                : getString(R.string.not_updated));

        binding.subIndicesSection.bind(station);

        binding.weatherWrapper.setVisibility(
                binding.weatherSection.bind(station) ? View.VISIBLE : View.GONE);
        binding.trendSection.bind(station);

        binding.errorCard.hide();
        binding.swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null && !Double.isNaN(lastAqi)) {
            binding.guidanceSection.bind(
                    AudienceService.INSTANCE.current(requireContext()), lastAqi);
        }
    }

    private void showError(String message) {
        binding.errorCard.show(message);
        binding.swipeRefresh.setRefreshing(false);

        binding.aqiHero.showError();
        lastAqi = Double.NaN;
        binding.guidanceSection.clear();
        binding.weatherWrapper.setVisibility(View.GONE);
        binding.trendSection.clear();
        binding.subIndicesSection.clear();
        binding.updatedText.setText(R.string.not_updated);
    }

    private String formatEpoch(long epochSeconds) {
        if (epochSeconds <= 0) {
            return "just now";
        }
        return DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.getDefault())
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochSecond(epochSeconds));
    }
}
