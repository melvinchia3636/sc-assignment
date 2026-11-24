package dev.melvinchia3636.airngreen.model;

/**
 * A park paired with its distance from a query point.
 *
 * @param park the green space
 * @param distanceKm great-circle distance in kilometres
 */
public record NearbyPark(Park park, double distanceKm) {
}
