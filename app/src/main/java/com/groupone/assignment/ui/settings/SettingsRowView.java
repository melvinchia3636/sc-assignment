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

    private final TextView title;
    private final TextView value;

    public SettingsRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_settings_row, this, true);
        title = findViewById(R.id.settings_row_title);
        value = findViewById(R.id.settings_row_value);

        TypedArray attributes = context.obtainStyledAttributes(
                attrs, R.styleable.SettingsRowView);
        CharSequence rowTitle = attributes.getText(R.styleable.SettingsRowView_rowTitle);
        if (rowTitle != null) {
            title.setText(rowTitle);
        }
        CharSequence rowValue = attributes.getText(R.styleable.SettingsRowView_rowValue);
        if (rowValue != null) {
            value.setText(rowValue);
        }
        attributes.recycle();
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
