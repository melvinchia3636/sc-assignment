package dev.melvinchia3636.airngreen.model;

/**
 * Air Quality Index bands, from best to worst.
 *
 * <p>The bands follow the United States Environmental Protection Agency scale
 * used by the upstream service: 0-50, 51-100, 101-150, 151-200, 201-300 and
 * above 300.</p>
 */
public enum AqiLevel {

    /** A reading that could not be interpreted. */
    UNKNOWN("Unknown", -1),

    /** Clean air. */
    GOOD("Good", 0),

    /** Acceptable air; unusually sensitive people should take care. */
    MODERATE("Moderate", 51),

    /** Members of sensitive groups may feel effects. */
    SENSITIVE("Unhealthy for sensitive groups", 101),

    /** Some members of the general public may feel effects. */
    UNHEALTHY("Unhealthy", 151),

    /** Health alert: everyone may experience serious effects. */
    VERY_UNHEALTHY("Very unhealthy", 201),

    /** Emergency conditions. */
    HAZARDOUS("Hazardous", 301);

    private static final AqiLevel[] ORDERED = {
            GOOD, MODERATE, SENSITIVE, UNHEALTHY, VERY_UNHEALTHY, HAZARDOUS
    };

    private final String label;
    private final int floor;

    AqiLevel(String label, int floor) {
        this.label = label;
        this.floor = floor;
    }

    /**
     * Classifies a numeric AQI reading.
     *
     * @param aqi reading to classify; may be NaN or negative
     * @return the matching band, or {@link #UNKNOWN} for an invalid reading
     */
    public static AqiLevel classify(double aqi) {
        if (!Double.isFinite(aqi) || aqi < 0) {
            return UNKNOWN;
        }

        AqiLevel result = GOOD;
        for (AqiLevel level : ORDERED) {
            if (aqi >= level.floor) {
                result = level;
            }
        }

        return result;
    }

    /**
     * Human-readable band name.
     *
     * @return the label shown in the user interface
     */
    public String label() {
        return label;
    }

    /**
     * Reports whether this band is worse than another band.
     *
     * @param other band to compare with
     * @return true when this band sits further down the scale
     */
    public boolean isWorseThan(AqiLevel other) {
        return ordinal() > other.ordinal();
    }

    /**
     * Reports whether this band reaches or exceeds another band.
     *
     * @param other minimum band
     * @return true when this band is the same or worse
     */
    public boolean isAtLeast(AqiLevel other) {
        return ordinal() >= other.ordinal();
    }
}
