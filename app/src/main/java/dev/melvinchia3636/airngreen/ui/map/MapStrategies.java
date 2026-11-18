package dev.melvinchia3636.airngreen.ui.map;

import android.content.Context;

import org.osmdroid.tileprovider.tilesource.ITileSource;

import dev.melvinchia3636.airngreen.R;
import dev.melvinchia3636.airngreen.scrapers.lib.ApiException;
import dev.melvinchia3636.airngreen.repositories.options.MapProvider;
import dev.melvinchia3636.airngreen.repositories.strategies.MapProviderStrategy;

/**
 * Resolves the tile strategy for the provider chosen in settings.
 */
final class MapStrategies {

    private static final MapProviderStrategy OPEN_STREET_MAP = new OpenStreetMapStrategy();
    private static final MapProviderStrategy GOOGLE_MAPS = new GoogleMapsStrategy();

    private MapStrategies() {
    }

    static MapProviderStrategy of(MapProvider provider) {
        return provider == MapProvider.GOOGLE_MAPS ? GOOGLE_MAPS : OPEN_STREET_MAP;
    }

    private static final class OpenStreetMapStrategy implements MapProviderStrategy {

        @Override
        public boolean requiresApiKey() {
            return false;
        }

        @Override
        public int attributionRes() {
            return 0;
        }

        @Override
        public ITileSource tileSource(String apiKey, boolean nightMode) {
            return nightMode ? MapTiles.DARK : MapTiles.LIGHT;
        }
    }

    private static final class GoogleMapsStrategy implements MapProviderStrategy {

        @Override
        public boolean requiresApiKey() {
            return true;
        }

        @Override
        public int attributionRes() {
            return R.string.map_attribution_google;
        }

        @Override
        public ITileSource tileSource(String apiKey, boolean nightMode) {
            return new GoogleTileSource(apiKey, nightMode);
        }

        @Override
        public void prepare(ITileSource source, Context context) throws ApiException {
            ((GoogleTileSource) source).startSession(context);
        }
    }
}
