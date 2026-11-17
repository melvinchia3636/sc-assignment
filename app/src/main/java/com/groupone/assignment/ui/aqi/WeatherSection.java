package com.groupone.assignment.ui.aqi;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.groupone.assignment.R;
import com.groupone.assignment.repositories.model.StationDetail;
import com.groupone.assignment.ui.common.SectionView;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;

/**
 * Weather section: four metrics bound from a station report.
 */
public class WeatherSection extends SectionView {

    private final WeatherItem[] items;

    public WeatherSection(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_weather_section, this, true);

        items = new WeatherItem[] {
                new WeatherItem(context, "t", "%.1f °C",
                        FontAwesome.Icon.faw_thermometer_half,
                        R.string.weather_temperature),
                new WeatherItem(context, "h", "%.0f %%",
                        GoogleMaterial.Icon.gmd_opacity,
                        R.string.weather_humidity),
                new WeatherItem(context, "p", "%.0f hPa",
                        GoogleMaterial.Icon.gmd_speed,
                        R.string.weather_pressure),
                new WeatherItem(context, "w", "%.1f km/h",
                        FontAwesome.Icon.faw_wind,
                        R.string.weather_wind),
        };

        LinearLayout top = findViewById(R.id.weather_row_top);
        LinearLayout bottom = findViewById(R.id.weather_row_bottom);
        addItem(top, items[0], true);
        addItem(top, items[1], false);
        addItem(bottom, items[2], true);
        addItem(bottom, items[3], false);
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

    private void addItem(LinearLayout row, WeatherItem item, boolean spaced) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        if (spaced) {
            params.setMarginEnd(Math.round(8 * getResources().getDisplayMetrics().density));
        }
        row.addView(item, params);
    }
}
