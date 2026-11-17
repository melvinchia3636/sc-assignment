package com.groupone.assignment.ui.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.groupone.assignment.R;

/**
 * One settings row: title, current value and an optional chevron.
 */
public class SettingsRowView extends LinearLayout {

    private final TextView title;
    private final TextView value;
    private final ImageView chevron;

    public SettingsRowView(Context context) {
        this(context, null);
    }

    public SettingsRowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingsRowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        int padding = dp(16);
        setPadding(padding, padding, padding, padding);
        LayoutInflater.from(context).inflate(R.layout.view_settings_row, this, true);
        title = findViewById(R.id.settings_row_title);
        value = findViewById(R.id.settings_row_value);
        chevron = findViewById(R.id.settings_row_chevron);
    }

    public SettingsRowView setTitle(@StringRes int titleRes) {
        title.setText(titleRes);
        return this;
    }

    public SettingsRowView setValue(@StringRes int valueRes) {
        value.setText(valueRes);
        return this;
    }

    public SettingsRowView setValue(CharSequence text) {
        value.setText(text);
        return this;
    }

    public SettingsRowView setChevronVisible(boolean visible) {
        chevron.setVisibility(visible ? VISIBLE : GONE);
        return this;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
