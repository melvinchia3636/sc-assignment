package com.groupone.assignment.ui.aqi;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.groupone.assignment.R;
import com.groupone.assignment.repositories.model.StationDetail;
import com.groupone.assignment.ui.common.AppIcons;
import com.groupone.assignment.ui.common.SectionView;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;

/**
 * Weather section: four metrics bound from a station report.
 */
public class WeatherSection extends SectionView {

    private final WeatherItem temperature;
    private final WeatherItem humidity;
    private final WeatherItem pressure;
    private final WeatherItem wind;

    public WeatherSection(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_weather_section, this, true);
        temperature = findViewById(R.id.weather_temp_item);
        humidity = findViewById(R.id.weather_humidity_item);
        pressure = findViewById(R.id.weather_pressure_item);
        wind = findViewById(R.id.weather_wind_item);

        temperature.setUp(
                AppIcons.drawable(context, FontAwesome.Icon.faw_thermometer_half, "#1B5E20", 18),
                context.getString(R.string.weather_temperature));
        humidity.setUp(
                AppIcons.drawable(context, GoogleMaterial.Icon.gmd_opacity, "#1B5E20", 18),
                context.getString(R.string.weather_humidity));
        pressure.setUp(
                AppIcons.drawable(context, GoogleMaterial.Icon.gmd_speed, "#1B5E20", 18),
                context.getString(R.string.weather_pressure));
        wind.setUp(
                AppIcons.drawable(context, FontAwesome.Icon.faw_wind, "#1B5E20", 18),
                context.getString(R.string.weather_wind));
    }

    public void bind(StationDetail station) {
        double temp = station.current("t");
        double humid = station.current("h");
        double press = station.current("p");
        double speed = station.current("w");

        if (Double.isNaN(temp) && Double.isNaN(humid)
                && Double.isNaN(press) && Double.isNaN(speed)) {
            clear();
            return;
        }

        temperature.setValue(temp, "%.1f °C");
        humidity.setValue(humid, "%.0f %%");
        pressure.setValue(press, "%.0f hPa");
        wind.setValue(speed, "%.1f km/h");
        setVisibility(View.VISIBLE);
    }

    public void clear() {
        setVisibility(View.GONE);
        temperature.clear();
        humidity.clear();
        pressure.clear();
        wind.clear();
    }
}
