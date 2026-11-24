package dev.melvinchia3636.airngreen.ui;

import java.awt.Color;

import dev.melvinchia3636.airngreen.model.AqiLevel;

/**
 * Colours used to make AQI bands recognisable.
 */
public final class AqiColors {

    private static final Color GOOD_COLOR = new Color(0x2E7D32);
    private static final Color MODERATE_COLOR = new Color(0x8D6E00);
    private static final Color SENSITIVE_COLOR = new Color(0xD84300);
    private static final Color UNHEALTHY_COLOR = new Color(0xC62828);
    private static final Color VERY_UNHEALTHY_COLOR = new Color(0x6A1B9A);
    private static final Color HAZARDOUS_COLOR = new Color(0x4E342E);
    private static final Color UNKNOWN_COLOR = new Color(0x616161);

    private AqiColors() {
    }

    /**
     * Colour for an AQI band.
     *
     * @param level band to colour
     * @return a foreground colour with enough contrast on light backgrounds
     */
    public static Color forLevel(AqiLevel level) {
        return switch (level) {
            case GOOD -> GOOD_COLOR;
            case MODERATE -> MODERATE_COLOR;
            case SENSITIVE -> SENSITIVE_COLOR;
            case UNHEALTHY -> UNHEALTHY_COLOR;
            case VERY_UNHEALTHY -> VERY_UNHEALTHY_COLOR;
            case HAZARDOUS -> HAZARDOUS_COLOR;
            case UNKNOWN -> UNKNOWN_COLOR;
        };
    }
}
