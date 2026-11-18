package dev.melvinchia3636.airngreen.repositories.options;

import androidx.annotation.StringRes;

import dev.melvinchia3636.airngreen.R;

/**
 * Map providers offered by the settings screen.
 */
public enum MapProvider {

    OPEN_STREET_MAP(R.string.map_provider_osm),
    GOOGLE_MAPS(R.string.map_provider_google);

    @StringRes
    private final int labelRes;

    MapProvider(@StringRes int labelRes) {
        this.labelRes = labelRes;
    }

    @StringRes
    public int labelRes() {
        return labelRes;
    }
}
