package dev.melvinchia3636.airngreen.model;

/**
 * One AQI sample in a station's recent history.
 *
 * @param epochSeconds sample time, in seconds since the Unix epoch
 * @param value AQI at that time
 */
public record SeriesPoint(long epochSeconds, double value) {
}
