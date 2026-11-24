package dev.melvinchia3636.airngreen.model;

import java.util.Optional;

/**
 * The AQI band at which the user wants to be alerted.
 *
 * <p>{@link #OFF} disables alerts; every other constant alerts when a reading
 * reaches its band or worse.</p>
 */
public enum AlertThreshold {

    /** Alerts are disabled. */
    OFF("Off", null),

    /** Alert from moderate air quality upward. */
    MODERATE("Moderate or worse", AqiLevel.MODERATE),

    /** Alert when sensitive groups are affected. */
    SENSITIVE("Sensitive groups or worse", AqiLevel.SENSITIVE),

    /** Alert when the general public is affected. */
    UNHEALTHY("Unhealthy or worse", AqiLevel.UNHEALTHY),

    /** Alert only in very unhealthy or hazardous air. */
    VERY_UNHEALTHY("Very unhealthy or worse", AqiLevel.VERY_UNHEALTHY);

    private final String label;
    private final AqiLevel level;

    AlertThreshold(String label, AqiLevel level) {
        this.label = label;
        this.level = level;
    }

    /**
     * Band that must be reached before an alert fires.
     *
     * @return the band, or empty when alerts are disabled
     */
    public Optional<AqiLevel> level() {
        return Optional.ofNullable(level);
    }

    /**
     * Reports whether this threshold can fire.
     *
     * @return true unless this is {@link #OFF}
     */
    public boolean isEnabled() {
        return level != null;
    }

    /**
     * Finds a threshold by its enum name.
     *
     * @param name stored name, possibly from a properties file
     * @return the matching threshold, or {@link #OFF} when unknown
     */
    public static AlertThreshold fromName(String name) {
        for (AlertThreshold threshold : values()) {
            if (threshold.name().equalsIgnoreCase(name)) {
                return threshold;
            }
        }

        return OFF;
    }

    @Override
    public String toString() {
        return label;
    }
}
