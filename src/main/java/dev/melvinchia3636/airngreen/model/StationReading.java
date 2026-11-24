package dev.melvinchia3636.airngreen.model;

import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

/**
 * A station's current reading plus its recent AQI history.
 *
 * @param idx         upstream station identifier
 * @param name        station or district name
 * @param aqi         current Air Quality Index, possibly NaN when absent
 * @param updatedAt   time of the reading, in seconds since the Unix epoch
 * @param pollutants  current pollutant values keyed by upstream name
 * @param series      recent AQI samples in ascending time order
 */
public record StationReading(
        String idx,
        String name,
        double aqi,
        long updatedAt,
        Map<String, Double> pollutants,
        List<SeriesPoint> series) {

    /**
     * Defensively copies the collections so a reading cannot be mutated.
     */
    public StationReading {
        pollutants = Map.copyOf(pollutants);
        series = List.copyOf(series);
    }

    /**
     * Classifies the current reading.
     *
     * @return the matching AQI band
     */
    public AqiLevel level() {
        return AqiLevel.classify(aqi);
    }

    /**
     * Looks up one pollutant from the current reading.
     *
     * @param key upstream pollutant name such as {@code pm25}
     * @return the value when present
     */
    public OptionalDouble pollutant(String key) {
        Double value = pollutants.get(key);
        return value == null ? OptionalDouble.empty() : OptionalDouble.of(value);
    }
}
