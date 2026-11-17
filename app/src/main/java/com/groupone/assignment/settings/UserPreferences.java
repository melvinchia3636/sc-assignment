package com.groupone.assignment.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.groupone.assignment.repositories.model.Audience;

/**
 * Persists app-level preferences such as the selected audience.
 */
public final class UserPreferences {

    private static final String PREFS_NAME = "user_preferences";
    private static final String KEY_AUDIENCE = "audience";

    private UserPreferences() {
    }

    public static Audience currentAudience(Context context) {
        String name = preferences(context).getString(KEY_AUDIENCE, Audience.GENERAL.name());
        try {
            return Audience.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException invalidValue) {
            return Audience.GENERAL;
        }
    }

    public static void setAudience(Context context, Audience audience) {
        preferences(context).edit().putString(KEY_AUDIENCE, audience.name()).apply();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
