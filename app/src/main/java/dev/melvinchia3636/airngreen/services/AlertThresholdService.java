package dev.melvinchia3636.airngreen.services;

import android.content.Context;
import android.content.SharedPreferences;

import dev.melvinchia3636.airngreen.repositories.options.AlertThreshold;

/**
 * Owns the selected alert threshold.
 */
public final class AlertThresholdService extends ChoiceService<AlertThreshold> {

    public static final AlertThresholdService INSTANCE = new AlertThresholdService();

    private static final String PREFS_NAME = "user_preferences";
    private static final String KEY_THRESHOLD = "alert_threshold";

    private AlertThresholdService() {
    }

    @Override
    public AlertThreshold[] options() {
        return AlertThreshold.values();
    }

    @Override
    public AlertThreshold current(Context context) {
        String name = preferences(context).getString(KEY_THRESHOLD,
                AlertThreshold.OFF.name());
        try {
            return AlertThreshold.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException invalidValue) {
            return AlertThreshold.OFF;
        }
    }

    @Override
    public void apply(Context context, AlertThreshold threshold) {
        preferences(context).edit().putString(KEY_THRESHOLD, threshold.name()).apply();
    }

    @Override
    public int labelRes(AlertThreshold value) {
        return value.labelRes();
    }

    private SharedPreferences preferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
