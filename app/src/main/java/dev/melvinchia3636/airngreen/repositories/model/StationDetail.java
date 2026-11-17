package dev.melvinchia3636.airngreen.repositories.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Full station report: current readings plus an hourly series.
 *
 * Values in {@link #getCurrent()} are AQI sub-indices, exactly as WAQI returns
 * them. Series values are aligned to one point per hour and may be missing.
 */
public final class StationDetail {

    private final String idx;
    private final String name;
    private final double aqi;
    private final long updatedAt;
    private final Map<String, Double> current;
    private final Map<String, List<SeriesPoint>> series;

    public StationDetail(String idx, String name, double aqi, long updatedAt,
                         Map<String, Double> current,
                         Map<String, List<SeriesPoint>> series) {
        this.idx = idx == null ? "" : idx;
        this.name = name == null ? "" : name;
        this.aqi = aqi;
        this.updatedAt = updatedAt;
        this.current = Collections.unmodifiableMap(new LinkedHashMap<>(current));
        this.series = Collections.unmodifiableMap(new LinkedHashMap<>(series));
    }

    public String getIdx() {
        return idx;
    }

    public String getName() {
        return name;
    }

    public double getAqi() {
        return aqi;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public Map<String, Double> getCurrent() {
        return current;
    }

    public Map<String, List<SeriesPoint>> getSeries() {
        return series;
    }

    public double current(String key) {
        Double value = current.get(key);
        return value == null ? Double.NaN : value;
    }

    public List<SeriesPoint> series(String key) {
        List<SeriesPoint> points = series.get(key);
        return points == null ? Collections.emptyList() : points;
    }

    /**
     * Series values with missing hours represented as {@link Double#NaN},
     * suitable for charts and cards.
     */
    public List<Double> values(String key) {
        List<SeriesPoint> points = series(key);
        List<Double> values = new java.util.ArrayList<>(points.size());
        for (SeriesPoint point : points) {
            values.add(point.isMissing() ? Double.NaN : point.getV());
        }
        return values;
    }
}
