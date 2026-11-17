package com.groupone.assignment.ui.aqi;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.groupone.assignment.R;
import com.groupone.assignment.repositories.model.StationDetail;
import com.groupone.assignment.ui.common.SectionView;
import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;

/**
 * Weather section: four metrics bound from a station report.
 */
public class WeatherSection extends SectionView {

    private static final String[] KEYS = {"t", "h", "p", "w"};
    private static final String[] FORMATS = {"%.1f °C", "%.0f %%", "%.0f hPa", "%.1f km/h"};
    private static final int[] LABELS = {
            R.string.weather_temperature,
            R.string.weather_humidity,
            R.string.weather_pressure,
            R.string.weather_wind,
    };
    private static final IIcon[] ICONS = {
            FontAwesome.Icon.faw_thermometer_half,
            GoogleMaterial.Icon.gmd_opacity,
            GoogleMaterial.Icon.gmd_speed,
            FontAwesome.Icon.faw_wind,
    };

    private final WeatherItem[] items;

    public WeatherSection(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_weather_section, this, true);
        items = new WeatherItem[]{
                findViewById(R.id.weather_temp_item),
                findViewById(R.id.weather_humidity_item),
                findViewById(R.id.weather_pressure_item),
                findViewById(R.id.weather_wind_item),
        };

        for (int index = 0; index < items.length; index++) {
            items[index].setUp(KEYS[index], FORMATS[index], ICONS[index], LABELS[index]);
        }
    }

    public void bind(StationDetail station) {
        boolean hasValue = false;
        for (WeatherItem item : items) {
            hasValue |= item.bind(station);
        }
        setVisibility(hasValue ? View.VISIBLE : View.GONE);
    }

    public void clear() {
        setVisibility(View.GONE);
        for (WeatherItem item : items) {
            item.clear();
        }
    }
}
