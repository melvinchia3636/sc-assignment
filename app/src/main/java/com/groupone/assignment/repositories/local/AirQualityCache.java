package com.groupone.assignment.repositories.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.groupone.assignment.repositories.Snapshot;

/**
 * Stores the last successful station reading so the app can keep showing data
 * when the network is unavailable.
 */
public class AirQualityCache {

    private static final String PREFS_NAME = "air_quality_cache";
    private static final String KEY_SNAPSHOT = "last_snapshot";

    private static final Gson GSON = new GsonBuilder()
            .serializeSpecialFloatingPointValues()
            .create();

    private final SharedPreferences preferences;

    public AirQualityCache(Context context) {
        this.preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void save(Snapshot snapshot) {
        preferences.edit().putString(KEY_SNAPSHOT, GSON.toJson(snapshot)).apply();
    }

    public Snapshot load() {
        String raw = preferences.getString(KEY_SNAPSHOT, null);
        if (raw == null) {
            return null;
        }
        try {
            Snapshot snapshot = GSON.fromJson(raw, Snapshot.class);
            if (snapshot == null || snapshot.getStationDetail() == null) {
                preferences.edit().remove(KEY_SNAPSHOT).apply();
                return null;
            }
            return snapshot;
        } catch (JsonParseException invalidCache) {
            preferences.edit().remove(KEY_SNAPSHOT).apply();
            return null;
        }
    }
}
