package com.groupone.assignment.ui.aqi.sections;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.groupone.assignment.R;

/**
 * Error card with a retry action.
 */
public class ErrorCard extends LinearLayout {

    private final TextView message;
    private final View retry;

    public ErrorCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_error_card, this, true);
        message = findViewById(R.id.error_text);
        retry = findViewById(R.id.retry_button);
        setVisibility(GONE);
    }

    public void setOnRetryListener(Runnable action) {
        retry.setOnClickListener(clicked -> action.run());
    }

    public void show(String text) {
        message.setText(text);
        setVisibility(VISIBLE);
    }

    public void hide() {
        setVisibility(GONE);
    }
}
