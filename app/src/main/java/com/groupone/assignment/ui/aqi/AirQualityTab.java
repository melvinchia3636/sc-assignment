package com.groupone.assignment.ui.aqi;

import android.graphics.Color;
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
import com.groupone.assignment.guidance.AqiLevel;
import com.groupone.assignment.guidance.AqiLevelStrategy;
import com.groupone.assignment.services.AudienceService;
import com.groupone.assignment.ui.MainViewModel;
import com.groupone.assignment.ui.common.AppIcons;
import com.groupone.assignment.ui.common.BaseTab;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;
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
        binding.retryButton.setOnClickListener(clicked -> viewModel.refresh());
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
        AqiLevelStrategy level = AqiLevel.classify(station.getAqi());
        int background = Color.parseColor(level.colourHex());
        int foreground = Color.parseColor(AqiScale.textColourFor(level.colourHex()));

        binding.aqiCard.setCardBackgroundColor(background);
        binding.stationName.setTextColor(foreground);
        binding.aqiValue.setTextColor(foreground);
        binding.aqiCategory.setTextColor(foreground);
        binding.aqiAdvice.setTextColor(foreground);
        binding.aqiFaceIcon.setImageDrawable(AppIcons.drawable(
                requireContext(), level.icon(), foreground, 18));

        binding.stationName.setText(station.getName().isEmpty()
                ? viewModel.getLocationService().placeName() : station.getName());
        binding.aqiValue.setText(AqiScale.format(station.getAqi()));
        binding.aqiCategory.setText(level.labelRes());
        binding.aqiAdvice.setText(level.adviceRes());
        binding.guidanceSection.bind(
                AudienceService.INSTANCE.current(requireContext()), station.getAqi());

        long updatedAt = station.getUpdatedAt();
        binding.updatedText.setText(updatedAt > 0
                ? getString(R.string.updated_at, formatEpoch(updatedAt))
                : getString(R.string.not_updated));

        binding.pm25Card.bind("PM2.5", station.current("pm25"), station.values("pm25"));
        binding.pm10Card.bind("PM10", station.current("pm10"), station.values("pm10"));
        binding.no2Card.bind("NO₂", station.current("no2"), station.values("no2"));
        binding.o3Card.bind("O₃", station.current("o3"), station.values("o3"));
        binding.so2Card.bind("SO₂", station.current("so2"), station.values("so2"));
        binding.coCard.bind("CO", station.current("co"), station.values("co"));

        binding.weatherSection.bind(station);
        binding.trendSection.bind(station);

        binding.errorCard.setVisibility(View.GONE);
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
        binding.errorText.setText(message);
        binding.errorCard.setVisibility(View.VISIBLE);
        binding.swipeRefresh.setRefreshing(false);

        binding.aqiValue.setText(R.string.dash);
        binding.aqiCategory.setText(R.string.unavailable);
        binding.aqiFaceIcon.setImageDrawable(AppIcons.drawable(
                requireContext(), FontAwesome.Icon.faw_meh, "#FFFFFF", 18));
        binding.aqiAdvice.setText(R.string.connection_advice);
        lastAqi = Double.NaN;
        binding.guidanceSection.clear();
        binding.aqiCard.setCardBackgroundColor(Color.parseColor("#5F665F"));
        binding.stationName.setText("");
        binding.weatherSection.setVisibility(View.GONE);
        binding.trendSection.clear();
        binding.pm25Card.clear();
        binding.pm10Card.clear();
        binding.no2Card.clear();
        binding.o3Card.clear();
        binding.so2Card.clear();
        binding.coCard.clear();
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
