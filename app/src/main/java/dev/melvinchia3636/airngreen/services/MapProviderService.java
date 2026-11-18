package dev.melvinchia3636.airngreen.services;

import android.content.Context;
import android.content.SharedPreferences;

import dev.melvinchia3636.airngreen.repositories.options.MapProvider;

/**
 * Owns the selected map provider.
 */
public final class MapProviderService extends ChoiceService<MapProvider> {

    public static final MapProviderService INSTANCE = new MapProviderService();

    private static final String PREFS_NAME = "user_preferences";
    private static final String KEY_PROVIDER = "map_provider";

    private MapProviderService() {
    }

    @Override
    public MapProvider[] options() {
        return MapProvider.values();
    }

    @Override
    public MapProvider current(Context context) {
        String name = preferences(context).getString(KEY_PROVIDER,
                MapProvider.OPEN_STREET_MAP.name());
        try {
            return MapProvider.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException invalidValue) {
            return MapProvider.OPEN_STREET_MAP;
        }
    }

    @Override
    public void apply(Context context, MapProvider provider) {
        preferences(context).edit().putString(KEY_PROVIDER, provider.name()).apply();
    }

    @Override
    public int labelRes(MapProvider value) {
        return value.labelRes();
    }

    private SharedPreferences preferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
