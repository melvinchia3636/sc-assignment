package dev.melvinchia3636.airngreen.repositories.options;

import androidx.annotation.StringRes;

import dev.melvinchia3636.airngreen.R;
import dev.melvinchia3636.airngreen.repositories.policy.AqiLevel;

/**
 * AQI bands at which the user wants to be alerted. OFF disables alerts.
 */
public enum AlertThreshold {

    OFF(null, R.string.alert_off),
    MODERATE(AqiLevel.MODERATE, R.string.alert_moderate),
    SENSITIVE(AqiLevel.SENSITIVE, R.string.alert_sensitive),
    UNHEALTHY(AqiLevel.UNHEALTHY, R.string.alert_unhealthy),
    VERY_UNHEALTHY(AqiLevel.VERY_UNHEALTHY, R.string.alert_very_unhealthy);

    private final AqiLevel level;
    @StringRes
    private final int labelRes;

    AlertThreshold(AqiLevel level, @StringRes int labelRes) {
        this.level = level;
        this.labelRes = labelRes;
    }

    public boolean isEnabled() {
        return level != null;
    }

    /** Lowest band that triggers an alert, or {@code null} when alerts are off. */
    public AqiLevel level() {
        return level;
    }

    @StringRes
    public int labelRes() {
        return labelRes;
    }
}
