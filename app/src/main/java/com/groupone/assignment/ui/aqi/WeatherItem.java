package com.groupone.assignment.ui.aqi;

import android.content.Context;
import android.util.AttributeSet;
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

    private String key;
    private String format;

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

    public void setUp(String key, String format, IIcon icon, @StringRes int labelRes) {
        this.key = key;
        this.format = format;
        this.icon.setImageDrawable(AppIcons.drawable(getContext(), icon, "#1B5E20", 18));
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

    public void clear() {
        value.setText(R.string.dash);
    }
}
