package dev.melvinchia3636.airngreen.repositories.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dev.melvinchia3636.airngreen.repositories.model.StationDetail;

/**
 * Stores the last successful station reading so the app can keep showing data
 * when the network is unavailable.
 */
public class AirQualityCache {

    private static final String PREFS_NAME = "air_quality_cache";
    private static final String KEY_STATION = "last_station";

    private static final Gson GSON = new GsonBuilder()
            .serializeSpecialFloatingPointValues()
            .create();

    private final SharedPreferences preferences;

    public AirQualityCache(Context context) {
        this.preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void save(StationDetail station) {
        preferences.edit().putString(KEY_STATION, GSON.toJson(station)).apply();
    }

    public StationDetail load() {
        String raw = preferences.getString(KEY_STATION, null);
        if (raw == null) {
            return null;
        }
        try {
            StationDetail station = GSON.fromJson(raw, StationDetail.class);
            if (station == null) {
                preferences.edit().remove(KEY_STATION).apply();
            }
            return station;
        } catch (JsonParseException invalidCache) {
            preferences.edit().remove(KEY_STATION).apply();
            return null;
        }
    }
}
