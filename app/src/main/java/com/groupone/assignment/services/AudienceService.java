package com.groupone.assignment.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.groupone.assignment.repositories.model.Audience;

/**
 * Owns the selected audience preference.
 */
public final class AudienceService {

    private static final String PREFS_NAME = "user_preferences";
    private static final String KEY_AUDIENCE = "audience";

    private AudienceService() {
    }

    public static Audience current(Context context) {
        String name = preferences(context).getString(KEY_AUDIENCE, Audience.GENERAL.name());
        try {
            return Audience.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException invalidValue) {
            return Audience.GENERAL;
        }
    }

    public static void apply(Context context, Audience audience) {
        preferences(context).edit().putString(KEY_AUDIENCE, audience.name()).apply();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
