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
import com.groupone.assignment.repositories.model.SeriesPoint;
import com.groupone.assignment.repositories.model.StationDetail;
import com.groupone.assignment.databinding.TabAqiBinding;
import com.groupone.assignment.ui.MainViewModel;
import com.groupone.assignment.guidance.Audience;
import com.groupone.assignment.services.AudienceService;
import com.groupone.assignment.ui.common.AppIcons;
import com.groupone.assignment.ui.common.BaseTab;
import com.groupone.assignment.ui.common.SparklineChart;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
        SparklineChart.clear(binding.aqiSparkline);
        setUpWeatherItems();
        observeViewModel();
    }

    private void setUpWeatherItems() {
        binding.weatherTempItem.setUp(
                AppIcons.drawable(requireContext(), FontAwesome.Icon.faw_thermometer_half,
                        "#1B5E20", 18),
                getString(R.string.weather_temperature));
        binding.weatherHumidityItem.setUp(
                AppIcons.drawable(requireContext(), GoogleMaterial.Icon.gmd_opacity,
                        "#1B5E20", 18),
                getString(R.string.weather_humidity));
        binding.weatherPressureItem.setUp(
                AppIcons.drawable(requireContext(), GoogleMaterial.Icon.gmd_speed,
                        "#1B5E20", 18),
                getString(R.string.weather_pressure));
        binding.weatherWindItem.setUp(
                AppIcons.drawable(requireContext(), FontAwesome.Icon.faw_wind,
                        "#1B5E20", 18),
                getString(R.string.weather_wind));
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
        AqiScale.Level level = AqiScale.classify(station.getAqi());
        int background = Color.parseColor(level.getColourHex());
        int foreground = Color.parseColor(level.getTextColourHex());

        binding.aqiCard.setCardBackgroundColor(background);
        binding.stationName.setTextColor(foreground);
        binding.aqiValue.setTextColor(foreground);
        binding.aqiCategory.setTextColor(foreground);
        binding.aqiAdvice.setTextColor(foreground);
        binding.aqiFaceIcon.setImageDrawable(AppIcons.drawable(
                requireContext(), level.getIcon(), foreground, 18));

        binding.stationName.setText(station.getName().isEmpty()
                ? viewModel.getLocationService().placeName() : station.getName());
        binding.aqiValue.setText(AqiScale.format(station.getAqi()));
        binding.aqiCategory.setText(level.getLabelRes());
        binding.aqiAdvice.setText(level.getAdviceRes());
        applyAudienceNote(station.getAqi());
        binding.audienceNote.setTextColor(foreground);

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

        renderWeather(station);
        renderTrend(station);

        binding.errorCard.setVisibility(View.GONE);
        binding.swipeRefresh.setRefreshing(false);
    }

    private void applyAudienceNote(double aqi) {
        if (Double.isNaN(aqi)) {
            binding.audienceNote.setVisibility(View.GONE);
            return;
        }

        Audience audience = AudienceService.INSTANCE.current(requireContext());
        int minutes = audience.outdoorMinutes(aqi);
        String limit = minutes > 0
                ? getString(R.string.audience_outdoor_limit, minutes)
                : getString(R.string.audience_outdoor_avoid);

        int noteRes = audience.noteRes(aqi);
        String note = noteRes == 0 ? limit : getString(noteRes) + "\n" + limit;
        binding.audienceNote.setText(note);
        binding.audienceNote.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null && !Double.isNaN(lastAqi)) {
            applyAudienceNote(lastAqi);
        }
    }

    private void renderWeather(StationDetail station) {
        double temperature = station.current("t");
        double humidity = station.current("h");
        double pressure = station.current("p");
        double wind = station.current("w");

        if (Double.isNaN(temperature) && Double.isNaN(humidity)
                && Double.isNaN(pressure) && Double.isNaN(wind)) {
            binding.weatherCard.setVisibility(View.GONE);
            return;
        }
        binding.weatherTempItem.setValue(formatWeather(temperature, "%.1f °C"));
        binding.weatherHumidityItem.setValue(formatWeather(humidity, "%.0f %%"));
        binding.weatherPressureItem.setValue(formatWeather(pressure, "%.0f hPa"));
        binding.weatherWindItem.setValue(formatWeather(wind, "%.1f km/h"));
        binding.weatherCard.setVisibility(View.VISIBLE);
    }

    private String formatWeather(double value, String format) {
        return Double.isNaN(value) ? "--"
                : String.format(Locale.getDefault(), format, value);
    }

    private void renderTrend(StationDetail station) {
        List<SeriesPoint> points = station.series("aqi");
        int from = Math.max(0, points.size() - 24);
        List<Double> recent = new ArrayList<>(station.values("aqi").subList(from, points.size()));
        long startEpoch = from < points.size() ? points.get(from).getT() : 0;
        int step = points.size() > 1
                ? (int) (points.get(1).getT() - points.get(0).getT())
                : 3600;
        SparklineChart.showTrend(binding.aqiSparkline, recent, startEpoch, step);

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        int valid = 0;
        for (double value : recent) {
            if (Double.isNaN(value)) {
                continue;
            }
            min = Math.min(min, value);
            max = Math.max(max, value);
            valid++;
        }
        if (valid < 2) {
            binding.trendRange.setText(R.string.not_enough_history);
            return;
        }
        binding.trendRange.setText(getString(R.string.aqi_range,
                Math.round(min), Math.round(max), recent.size()));
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
        binding.audienceNote.setVisibility(View.GONE);
        binding.aqiCard.setCardBackgroundColor(Color.parseColor("#5F665F"));
        binding.stationName.setText("");
        binding.weatherCard.setVisibility(View.GONE);
        binding.weatherTempItem.clear();
        binding.weatherHumidityItem.clear();
        binding.weatherPressureItem.clear();
        binding.weatherWindItem.clear();
        SparklineChart.clear(binding.aqiSparkline);
        binding.pm25Card.clear();
        binding.pm10Card.clear();
        binding.no2Card.clear();
        binding.o3Card.clear();
        binding.so2Card.clear();
        binding.coCard.clear();
        binding.trendRange.setText("");
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
