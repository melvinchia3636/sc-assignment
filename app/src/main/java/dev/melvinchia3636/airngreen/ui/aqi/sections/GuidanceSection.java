package dev.melvinchia3636.airngreen.ui.aqi.sections;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import dev.melvinchia3636.airngreen.R;
import dev.melvinchia3636.airngreen.guidance.Audience;

/**
 * Audience-specific guidance card: note plus outdoor-time recommendation.
 */
public class GuidanceSection extends LinearLayout {

    private final TextView prefix;
    private final TextView label;
    private final TextView suffix;
    private final TextView note;
    private final TextView time;
    private double aqi = Double.NaN;

    public GuidanceSection(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_guidance_section, this, true);

        prefix = findViewById(R.id.guidance_prefix);
        label = findViewById(R.id.guidance_label);
        suffix = findViewById(R.id.guidance_suffix);
        note = findViewById(R.id.guidance_note);
        time = findViewById(R.id.guidance_time);

        bindOptional(prefix, R.string.guidance_prefix);
        bindOptional(suffix, R.string.guidance_suffix);
    }

    public void bind(Audience audience, double aqi) {
        if (Double.isNaN(aqi)) {
            clear();
            return;
        }

        this.aqi = aqi;
        label.setText(audience.labelRes());
        bindNote(audience.noteRes(aqi));
        bindTime(audience.outdoorMinutes(aqi));
        setVisibility(View.VISIBLE);
    }

    /** Re-renders the current reading for a different audience. */
    public void bindAudience(Audience audience) {
        bind(audience, aqi);
    }

    private void bindOptional(TextView view, int textRes) {
        String text = getContext().getString(textRes);
        view.setText(text);
        view.setVisibility(text.isEmpty() ? View.GONE : View.VISIBLE);
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
        aqi = Double.NaN;
        setVisibility(View.GONE);
    }
}
