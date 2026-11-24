package dev.melvinchia3636.airngreen.model;

/**
 * A station in the public station directory.
 *
 * @param idx       upstream station identifier
 * @param name      station name
 * @param latitude  station latitude in degrees
 * @param longitude station longitude in degrees
 * @param aqi       last reported AQI, or null when the station is idle
 */
public record StationSummary(
        String idx,
        String name,
        double latitude,
        double longitude,
        Double aqi) {
}
