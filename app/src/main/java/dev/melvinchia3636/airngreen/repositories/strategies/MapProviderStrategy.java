package dev.melvinchia3636.airngreen.repositories.strategies;

import android.content.Context;

import androidx.annotation.StringRes;

import org.osmdroid.tileprovider.tilesource.ITileSource;

import dev.melvinchia3636.airngreen.repositories.ApiException;

/**
 * Strategy for one map provider: how its tiles are built and attributed, and
 * any blocking preparation they need before they can load.
 */
public interface MapProviderStrategy {

    /** Whether the provider cannot render without a user-supplied API key. */
    boolean requiresApiKey();

    /** Attribution to show over the map, or 0 when the provider needs none. */
    @StringRes
    int attributionRes();

    ITileSource tileSource(String apiKey, boolean nightMode);

    /**
     * Blocking preparation for the given tile source, run off the main thread.
     * Google uses it to create a tile session; OpenStreetMap needs nothing.
     */
    default void prepare(ITileSource source, Context context) throws ApiException {
    }
}
