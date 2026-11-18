package dev.melvinchia3636.airngreen.services;

import android.content.Context;
import android.content.SharedPreferences;

import dev.melvinchia3636.airngreen.repositories.policy.Audience;

/**
 * Owns the selected audience preference.
 */
public final class AudienceService extends ChoiceService<Audience> {

    public static final AudienceService INSTANCE = new AudienceService();

    private static final String PREFS_NAME = "user_preferences";
    private static final String KEY_AUDIENCE = "audience";

    private AudienceService() {
    }

    @Override
    public Audience[] options() {
        return Audience.values();
    }

    @Override
    public Audience current(Context context) {
        String name = preferences(context).getString(KEY_AUDIENCE, Audience.GENERAL.name());
        try {
            return Audience.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException invalidValue) {
            return Audience.GENERAL;
        }
    }

    @Override
    public void apply(Context context, Audience audience) {
        preferences(context).edit().putString(KEY_AUDIENCE, audience.name()).apply();
    }

    @Override
    public int labelRes(Audience value) {
        return value.labelRes();
    }

    private SharedPreferences preferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
