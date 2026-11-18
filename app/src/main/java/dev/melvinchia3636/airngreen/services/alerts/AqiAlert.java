package dev.melvinchia3636.airngreen.services.alerts;

import dev.melvinchia3636.airngreen.repositories.policy.AqiLevel;

/**
 * One pollution-threshold crossing, delivered to every registered observer.
 */
public record AqiAlert(AqiLevel level, double aqi, String stationName, long epochMillis) {
}
