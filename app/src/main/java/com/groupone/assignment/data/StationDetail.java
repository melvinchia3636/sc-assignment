package com.groupone.assignment.data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Detailed reading for one WAQI (aqicn) station: the current sub-indices, the
 * 72-hour aligned series and the latest weather observation.
 *
 * Values in {@link #getCurrent()} are AQI sub-indices, exactly as WAQI returns
 * them. Series values are aligned to one point per hour and may contain NaN.
 */
public final class StationDetail {

    private final String idx;
    private final String name;
    private final double aqi;
    private final long start;
    private final int step;
    private final long updatedAt;
    private final Map<String, Double> current;
    private final Map<String, List<Double>> series;

    public StationDetail(String idx, String name, double aqi, long start, int step,
                         long updatedAt, Map<String, Double> current,
                         Map<String, List<Double>> series) {
        this.idx = idx == null ? "" : idx;
        this.name = name == null ? "" : name;
        this.aqi = aqi;
        this.start = start;
        this.step = step;
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

    public long getStart() {
        return start;
    }

    public int getStep() {
        return step;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public Map<String, Double> getCurrent() {
        return current;
    }

    public Map<String, List<Double>> getSeries() {
        return series;
    }

    public double current(String key) {
        Double value = current.get(key);
        return value == null ? Double.NaN : value;
    }

    public List<Double> series(String key) {
        List<Double> values = series.get(key);
        return values == null ? Collections.emptyList() : values;
    }
}
