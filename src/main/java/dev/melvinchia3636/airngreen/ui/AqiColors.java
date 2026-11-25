package dev.melvinchia3636.airngreen.ui;

import dev.melvinchia3636.airngreen.model.AqiLevel;

import java.awt.Color;
import java.util.Map;

/** Colours used to make AQI bands recognisable. */
public final class AqiColors {

    private static final Color UNKNOWN_COLOR = new Color(0x616161);
    private static final Map<AqiLevel, Color> PALETTE = Map.ofEntries(
        Map.entry(AqiLevel.GOOD, new Color(0x2E7D32)),
        Map.entry(AqiLevel.MODERATE, new Color(0x8D6E00)),
        Map.entry(AqiLevel.SENSITIVE, new Color(0xD84300)),
        Map.entry(AqiLevel.UNHEALTHY, new Color(0xC62828)),
        Map.entry(AqiLevel.VERY_UNHEALTHY, new Color(0x6A1B9A)),
        Map.entry(AqiLevel.HAZARDOUS, new Color(0x4E342E)),
        Map.entry(AqiLevel.UNKNOWN, UNKNOWN_COLOR)
    );

    private AqiColors() {
    }

    /**
     * Colour for an AQI band.
     *
     * @param level band to colour
     * @return a foreground colour with enough contrast on light backgrounds
     */
    public static Color forLevel(AqiLevel level) {
        if (level == null) {
            return UNKNOWN_COLOR;
        }

        return PALETTE.getOrDefault(level, UNKNOWN_COLOR);
    }

    /**
     * Colour for a user-facing notice.
     *
     * @param warning true when the notice reports a problem
     * @return the warning colour, or the good colour otherwise
     */
    public static Color forMessage(boolean warning) {
        return forLevel(warning ? AqiLevel.SENSITIVE : AqiLevel.GOOD);
    }
}
