package com.groupone.assignment.ui.aqi.sections;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.groupone.assignment.R;
import com.groupone.assignment.repositories.model.StationDetail;
import com.groupone.assignment.ui.aqi.WeatherItem;
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
                        FontAwesome.Icon.faw_thermometer_half, R.string.weather_temperature),
                new WeatherItem(context, "h", "%.0f %%",
                        GoogleMaterial.Icon.gmd_opacity, R.string.weather_humidity),
                new WeatherItem(context, "p", "%.0f hPa",
                        GoogleMaterial.Icon.gmd_speed, R.string.weather_pressure),
                new WeatherItem(context, "w", "%.1f km/h",
                        FontAwesome.Icon.faw_wind, R.string.weather_wind),
        };

        LinearLayout[] rows = {
                findViewById(R.id.weather_row_top),
                findViewById(R.id.weather_row_bottom),
        };

        placeItems(items, rows);
    }

    public void bind(StationDetail station) {
        boolean hasValue = false;

        for (WeatherItem item : items) {
            hasValue |= item.bind(station);
        }

        setVisibility(hasValue ? View.VISIBLE : View.GONE);
    }

    private void placeItems(WeatherItem[] items, LinearLayout[] rows) {
        for (int index = 0; index < items.length; index++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            if (index % 2 == 0) {
                params.setMarginEnd(Math.round(8 * getResources().getDisplayMetrics().density));
            }
            rows[index / 2].addView(items[index], params);
        }
    }
}
