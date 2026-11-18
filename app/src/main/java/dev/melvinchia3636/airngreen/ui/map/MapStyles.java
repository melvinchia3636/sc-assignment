package dev.melvinchia3636.airngreen.ui.map;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import dev.melvinchia3636.airngreen.R;

/**
 * Loads the map style arrays kept as raw resources.
 */
final class MapStyles {

    private MapStyles() {
    }

    /** Google's published Night style array, or {@code null} if unreadable. */
    static String night(Context context) {
        try (InputStream stream = context.getResources()
                .openRawResource(R.raw.map_style_night)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            return json.toString();
        } catch (IOException unreadable) {
            return null;
        }
    }
}
