package dev.melvinchia3636.airngreen.model;

import dev.melvinchia3636.airngreen.model.AqiLevel;

/**
 * A pollution alert delivered to every registered observer.
 *
 * @param level       AQI band that triggered the alert
 * @param aqi         numeric AQI at the time
 * @param stationName station or district that reported it
 * @param createdAt   time the alert was raised, in milliseconds
 */
public record AqiAlert(
        AqiLevel level,
        double aqi,
        String stationName,
        long createdAt) {
}
