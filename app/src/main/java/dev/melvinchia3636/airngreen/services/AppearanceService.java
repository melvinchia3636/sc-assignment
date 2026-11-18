package dev.melvinchia3636.airngreen.services;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import dev.melvinchia3636.airngreen.repositories.options.Appearance;

/**
 * Owns the selected appearance and applies it to the whole app.
 */
public final class AppearanceService extends ChoiceService<Appearance> {

    public static final AppearanceService INSTANCE = new AppearanceService();

    private static final String PREFS_NAME = "user_preferences";
    private static final String KEY_APPEARANCE = "appearance";

    private AppearanceService() {
    }

    @Override
    public Appearance[] options() {
        return Appearance.values();
    }

    @Override
    public Appearance current(Context context) {
        String name = preferences(context).getString(KEY_APPEARANCE, Appearance.SYSTEM.name());
        try {
            return Appearance.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException invalidValue) {
            return Appearance.SYSTEM;
        }
    }

    @Override
    public void apply(Context context, Appearance appearance) {
        preferences(context).edit().putString(KEY_APPEARANCE, appearance.name()).apply();
        AppCompatDelegate.setDefaultNightMode(nightMode(appearance));
    }

    /** Restores the stored appearance before the activity is created. */
    public void restore(Context context) {
        AppCompatDelegate.setDefaultNightMode(nightMode(current(context)));
    }

    @Override
    public int labelRes(Appearance value) {
        return value.labelRes();
    }

    private static int nightMode(Appearance appearance) {
        switch (appearance) {
            case LIGHT:
                return AppCompatDelegate.MODE_NIGHT_NO;
            case DARK:
                return AppCompatDelegate.MODE_NIGHT_YES;
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }

    private SharedPreferences preferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
