package dev.melvinchia3636.airngreen.services;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Stores user-supplied API keys in Keystore-backed encrypted preferences.
 */
public final class ApiKeyStore {

    private static final String PREFS_NAME = "secure_preferences";
    private static final String KEY_GOOGLE_MAPS = "google_maps_api_key";

    private ApiKeyStore() {
    }

    public static String googleMapsKey(Context context) {
        return encryptedPreferences(context).getString(KEY_GOOGLE_MAPS, "");
    }

    public static boolean hasGoogleMapsKey(Context context) {
        return !googleMapsKey(context).isEmpty();
    }

    public static void saveGoogleMapsKey(Context context, String key) {
        String trimmed = key == null ? "" : key.trim();
        SharedPreferences.Editor editor = encryptedPreferences(context).edit();
        if (trimmed.isEmpty()) {
            editor.remove(KEY_GOOGLE_MAPS);
        } else {
            editor.putString(KEY_GOOGLE_MAPS, trimmed);
        }
        editor.apply();
    }

    private static SharedPreferences encryptedPreferences(Context context) {
        Context applicationContext = context.getApplicationContext();
        try {
            MasterKey masterKey = new MasterKey.Builder(applicationContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            return EncryptedSharedPreferences.create(
                    applicationContext,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException | IOException failure) {
            throw new IllegalStateException("Secure preferences unavailable", failure);
        }
    }
}
