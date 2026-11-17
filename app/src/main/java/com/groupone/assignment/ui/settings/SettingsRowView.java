package com.groupone.assignment.ui.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.groupone.assignment.R;

/**
 * One settings row: title, current value and an optional chevron.
 */
public class SettingsRowView extends LinearLayout {

    private final TextView value;

    public SettingsRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_settings_row, this, true);

        TextView title = findViewById(R.id.settings_row_title);
        value = findViewById(R.id.settings_row_value);

        TypedArray attributes = context.obtainStyledAttributes(
                attrs, R.styleable.SettingsRowView);

        try {
            applyText(title, attributes, R.styleable.SettingsRowView_rowTitle);
            applyText(value, attributes, R.styleable.SettingsRowView_rowValue);
        } finally {
            attributes.recycle();
        }
    }

    private static void applyText(TextView view, TypedArray attributes, int index) {
        CharSequence text = attributes.getText(index);

        if (text != null) {
            view.setText(text);
        }
    }

    public SettingsRowView setValue(@StringRes int valueRes) {
        value.setText(valueRes);
        return this;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        findViewById(R.id.settings_row_chevron).setVisibility(isClickable() ? VISIBLE : GONE);
    }
}
