package com.groupone.assignment.ui.aqi;

import androidx.annotation.StringRes;

import com.groupone.assignment.R;
import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;

/**
 * Weather measurements shown on the air-quality screen.
 */
public enum WeatherMetric {

    TEMPERATURE("t", FontAwesome.Icon.faw_thermometer_half,
            R.string.weather_temperature, "%.1f °C"),
    HUMIDITY("h", GoogleMaterial.Icon.gmd_opacity,
            R.string.weather_humidity, "%.0f %%"),
    PRESSURE("p", GoogleMaterial.Icon.gmd_speed,
            R.string.weather_pressure, "%.0f hPa"),
    WIND("w", FontAwesome.Icon.faw_wind,
            R.string.weather_wind, "%.1f km/h");

    private final String key;
    private final IIcon icon;
    private final int labelRes;
    private final String format;

    WeatherMetric(String key, IIcon icon, @StringRes int labelRes, String format) {
        this.key = key;
        this.icon = icon;
        this.labelRes = labelRes;
        this.format = format;
    }

    public String key() {
        return key;
    }

    public IIcon icon() {
        return icon;
    }

    @StringRes
    public int labelRes() {
        return labelRes;
    }

    public String format() {
        return format;
    }
}
