package com.groupone.assignment.ui.aqi;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.groupone.assignment.R;
import com.groupone.assignment.repositories.model.StationDetail;
import com.groupone.assignment.ui.common.AppIcons;

import java.util.Locale;

/**
 * One weather metric: icon, value and label.
 */
public class WeatherItem extends LinearLayout {

    private final ImageView icon;
    private final TextView value;
    private final TextView label;

    private WeatherMetric metric;

    public WeatherItem(Context context) {
        this(context, null);
    }

    public WeatherItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeatherItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_weather_item, this, true);
        icon = findViewById(R.id.weather_icon);
        value = findViewById(R.id.weather_value);
        label = findViewById(R.id.weather_label);
    }

    public void setUp(WeatherMetric metric) {
        this.metric = metric;
        icon.setImageDrawable(AppIcons.drawable(getContext(), metric.icon(), "#1B5E20", 18));
        label.setText(metric.labelRes());
    }

    /** Binds the metric value and returns whether the station reported it. */
    public boolean bind(StationDetail station) {
        double reading = station.current(metric.key());
        value.setText(Double.isNaN(reading)
                ? getContext().getString(R.string.dash)
                : String.format(Locale.getDefault(), metric.format(), reading));
        return !Double.isNaN(reading);
    }

    public void clear() {
        value.setText(R.string.dash);
    }
}
