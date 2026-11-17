package com.groupone.assignment.ui.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
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
        LayoutInflater.from(context).inflate(R.layout.view_settings_row, this, true);
        title = findViewById(R.id.settings_row_title);
        value = findViewById(R.id.settings_row_value);
        chevron = findViewById(R.id.settings_row_chevron);

        TypedArray attributes = context.obtainStyledAttributes(
                attrs, R.styleable.SettingsRowView, defStyleAttr, 0);
        CharSequence rowTitle = attributes.getText(R.styleable.SettingsRowView_rowTitle);
        if (rowTitle != null) {
            title.setText(rowTitle);
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

        if (chevron != null) {
            chevron.setVisibility(isClickable() ? VISIBLE : GONE);
        }
    }

}
