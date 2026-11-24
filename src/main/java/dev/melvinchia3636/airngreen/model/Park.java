package dev.melvinchia3636.airngreen.model;

/**
 * A public green space in the monitor's dataset.
 *
 * @param name park name
 * @param district district the park belongs to
 * @param latitude park latitude in degrees
 * @param longitude park longitude in degrees
 * @param facilities short description of what the park offers
 */
public record Park(
    String name,
    District district,
    double latitude,
    double longitude,
    String facilities
) {
}
