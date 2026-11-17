package com.groupone.assignment.ui.aqi;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.groupone.assignment.R;
import com.groupone.assignment.repositories.model.StationDetail;
import com.groupone.assignment.ui.common.SectionView;

/**
 * Weather section: four metrics bound from a station report.
 */
public class WeatherSection extends SectionView {

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

        WeatherMetric[] metrics = WeatherMetric.values();
        for (int index = 0; index < items.length; index++) {
            items[index].setUp(metrics[index]);
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
