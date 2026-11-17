package com.groupone.assignment.ui.aqi;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.groupone.assignment.R;
import com.groupone.assignment.repositories.model.StationDetail;
import com.groupone.assignment.ui.common.AppIcons;
import com.mikepenz.iconics.typeface.IIcon;

import java.util.Locale;

/**
 * One weather metric: icon, value and label.
 */
public class WeatherItem extends LinearLayout {

    private final ImageView icon;
    private final TextView value;
    private final TextView label;
    private final String key;
    private final String format;

    public WeatherItem(Context context, String key, String format,
                       IIcon metricIcon, @StringRes int labelRes) {
        super(context);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_weather_item, this, true);

        this.key = key;
        this.format = format;
        icon = findViewById(R.id.weather_icon);
        value = findViewById(R.id.weather_value);
        label = findViewById(R.id.weather_label);

        icon.setImageDrawable(AppIcons.drawable(context, metricIcon, "#1B5E20", 18));
        label.setText(labelRes);
    }

    /** Binds the metric value and returns whether the station reported it. */
    public boolean bind(StationDetail station) {
        double reading = station.current(key);
        value.setText(Double.isNaN(reading)
                ? getContext().getString(R.string.dash)
                : String.format(Locale.getDefault(), format, reading));
        return !Double.isNaN(reading);
    }
}
