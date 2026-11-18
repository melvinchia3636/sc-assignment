package dev.melvinchia3636.airngreen.ui.map;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.MapTileIndex;

/**
 * Tile sources used by the station map: OSM for light, Esri Dark Gray for dark.
 */
final class MapTiles {

    static final ITileSource LIGHT = TileSourceFactory.MAPNIK;

    static final ITileSource DARK = new EsriDarkGray();

    private MapTiles() {
    }

    /**
     * Esri's Dark Gray Canvas, which uses a {@code /{z}/{y}/{x}} path and needs
     * no API key.
     */
    private static final class EsriDarkGray extends OnlineTileSourceBase {

        private static final String BASE_URL = "https://services.arcgisonline.com"
                + "/ArcGIS/rest/services/Canvas/World_Dark_Gray_Base/MapServer/tile/";

        EsriDarkGray() {
            super("EsriDarkGray", 0, 16, 256, ".png", new String[]{BASE_URL},
                    "Esri, HERE, Garmin, © OpenStreetMap contributors");
        }

        @Override
        public String getTileURLString(long mapTileIndex) {
            return getBaseUrl()
                    + MapTileIndex.getZoom(mapTileIndex) + "/"
                    + MapTileIndex.getY(mapTileIndex) + "/"
                    + MapTileIndex.getX(mapTileIndex);
        }
    }
}
