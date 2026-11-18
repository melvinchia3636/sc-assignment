package dev.melvinchia3636.airngreen.ui.map;

import android.content.Context;

import dev.melvinchia3636.airngreen.repositories.ApiException;
import dev.melvinchia3636.airngreen.repositories.remote.GoogleTilesRepository;
import dev.melvinchia3636.airngreen.services.AppSigning;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.MapTileIndex;

/**
 * Google Map Tiles API tiles, authorised by a user-supplied key and a session
 * token created for that key.
 */
final class GoogleTileSource extends OnlineTileSourceBase {

    private final String apiKey;
    private final boolean nightMode;
    private volatile String session = "";

    GoogleTileSource(String apiKey, boolean nightMode) {
        // A distinct name keeps the light and dark tiles in separate caches.
        super(nightMode ? "GoogleMapsNight" : "GoogleMaps", 0, 22, 256, ".png",
                new String[]{"https://tile.googleapis.com/v1/2dtiles/"}, "Google");
        this.apiKey = apiKey;
        this.nightMode = nightMode;
    }

    /** Blocking: creates the Map Tiles session this source needs. */
    public void startSession(Context context) throws ApiException {
        String styleJson = nightMode ? MapStyles.night(context) : null;
        GoogleTilesRepository.Session created = GoogleTilesRepository.createSession(
                apiKey, AppSigning.googleKeyHeaders(context), styleJson);
        session = created.token();
    }

    void setSession(String token) {
        session = token;
    }

    @Override
    public String getTileURLString(long mapTileIndex) {
        return getBaseUrl()
                + MapTileIndex.getZoom(mapTileIndex) + "/"
                + MapTileIndex.getX(mapTileIndex) + "/"
                + MapTileIndex.getY(mapTileIndex)
                + "?session=" + session
                + "&key=" + apiKey;
    }
}
