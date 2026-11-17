package com.groupone.assignment.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Persists the selected audience.
 */
public final class AudienceSettings {

    private static final String PREFS_NAME = "user_preferences";
    private static final String KEY_AUDIENCE = "audience";

    private AudienceSettings() {
    }

    public static Audience current(Context context) {
        String name = preferences(context).getString(KEY_AUDIENCE, Audience.GENERAL.name());
        try {
            return Audience.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException invalidValue) {
            return Audience.GENERAL;
        }
    }

    public static void set(Context context, Audience audience) {
        preferences(context).edit().putString(KEY_AUDIENCE, audience.name()).apply();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
