package com.groupone.assignment.ui.aqi;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.groupone.assignment.R;

/**
 * One weather metric: icon, value and label.
 */
public class WeatherItem extends LinearLayout {

    private final ImageView icon;
    private final TextView value;
    private final TextView label;

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

    public void setUp(Drawable drawable, CharSequence labelText) {
        icon.setImageDrawable(drawable);
        label.setText(labelText);
    }

    public void setValue(CharSequence text) {
        value.setText(text);
    }

    public void clear() {
        value.setText(R.string.dash);
    }
}
