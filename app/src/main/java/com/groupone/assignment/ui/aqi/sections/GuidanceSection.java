package com.groupone.assignment.ui.aqi.sections;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.groupone.assignment.R;
import com.groupone.assignment.guidance.Audience;

/**
 * Audience-specific guidance card: note plus outdoor-time recommendation.
 */
public class GuidanceSection extends LinearLayout {

    private final TextView label;
    private final TextView note;
    private final TextView time;

    public GuidanceSection(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_guidance_section, this, true);

        label = findViewById(R.id.guidance_label);
        note = findViewById(R.id.guidance_note);
        time = findViewById(R.id.guidance_time);
    }

    public void bind(Audience audience, double aqi) {
        if (Double.isNaN(aqi)) {
            clear();
            return;
        }

        label.setText(audience.labelRes());
        bindNote(audience.noteRes(aqi));
        bindTime(audience.outdoorMinutes(aqi));

        setVisibility(View.VISIBLE);
    }

    private void bindNote(int noteRes) {
        if (noteRes == 0) {
            note.setVisibility(View.GONE);
            return;
        }

        note.setText(noteRes);
        note.setVisibility(View.VISIBLE);
    }

    private void bindTime(int minutes) {
        time.setText(minutes > 0
                ? getContext().getString(R.string.audience_outdoor_limit, minutes)
                : getContext().getString(R.string.audience_outdoor_avoid));
    }

    public void clear() {
        setVisibility(View.GONE);
    }
}
