package dev.melvinchia3636.airngreen.model;

/**
 * A city district the monitor can report on.
 *
 * <p>
 * Each district carries the coordinates used for the nearest-station query and
 * for ranking nearby parks.
 */
public enum District {

    /** Kuala Lumpur city centre. */
    KUALA_LUMPUR("Kuala Lumpur", 3.1390, 101.6869),

    /** Petaling Jaya, Selangor. */
    PETALING_JAYA("Petaling Jaya", 3.1073, 101.6067),

    /** Shah Alam, Selangor. */
    SHAH_ALAM("Shah Alam", 3.0733, 101.5185),

    /** Klang, Selangor. */
    KLANG("Klang", 3.0449, 101.4455),

    /** Putrajaya federal territory. */
    PUTRAJAYA("Putrajaya", 2.9264, 101.6964),

    /** Cyberjaya, Selangor. */
    CYBERJAYA("Cyberjaya", 2.9213, 101.6559);

    private final String label;
    private final double latitude;
    private final double longitude;

    District(String label, double latitude, double longitude) {
        this.label = label;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Display name used in combo boxes and reports.
     *
     * @return the district name
     */
    public String label() {
        return label;
    }

    /**
     * Latitude used as the query origin.
     *
     * @return degrees north
     */
    public double latitude() {
        return latitude;
    }

    /**
     * Longitude used as the query origin.
     *
     * @return degrees east
     */
    public double longitude() {
        return longitude;
    }

    /**
     * Finds a district by its enum name.
     *
     * @param name stored name, possibly from a properties file
     * @return the matching district, or {@link #KUALA_LUMPUR} when unknown
     */
    public static District fromName(String name) {
        for (District district : values()) {
            if (district.name().equalsIgnoreCase(name)) {
                return district;
            }
        }

        return KUALA_LUMPUR;
    }

    @Override
    public String toString() {
        return label;
    }
}
