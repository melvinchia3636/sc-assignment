package com.groupone.assignment.ui.aqi.sections;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.groupone.assignment.R;
import com.groupone.assignment.repositories.model.StationDetail;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Screen header: place name and last-updated timestamp.
 */
public class HeaderSection extends LinearLayout {

    private final TextView cityTitle;
    private final TextView updatedText;

    public HeaderSection(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_header_section, this, true);

        cityTitle = findViewById(R.id.city_title);
        updatedText = findViewById(R.id.updated_text);
    }

    public void bind(StationDetail station) {
        long updatedAt = station.getUpdatedAt();
        updatedText.setText(updatedAt > 0
                ? getContext().getString(R.string.updated_at, formatEpoch(updatedAt))
                : getContext().getString(R.string.not_updated));
    }

    public void setUnavailable() {
        updatedText.setText(R.string.not_updated);
    }

    private String formatEpoch(long epochSeconds) {
        return DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.getDefault())
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochSecond(epochSeconds));
    }
}
