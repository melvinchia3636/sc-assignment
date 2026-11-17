package com.groupone.assignment.lib;

import com.groupone.assignment.repositories.StationRepository.ObservationSeries;
import com.groupone.assignment.repositories.StationRepository.Point;
import com.groupone.assignment.repositories.StationRepository.SeriesPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Series resampling and combination shared by the station parsers.
 */
public final class Series {

    public static final int DEFAULT_SERIES_HOURS = 72;
    public static final int DEFAULT_SERIES_STEP = 3600;

    private Series() {
    }

    /** Returns the newest timestamp across all series, or {@link Long#MIN_VALUE}. */
    public static long newestSeriesTime(List<ObservationSeries> input) {
        long newest = Long.MIN_VALUE;
        for (ObservationSeries series : input) {
            for (Point point : series.getPoints()) {
                newest = Math.max(newest, point.getT());
            }
        }
        return newest;
    }

    /**
     * Resamples series onto a uniform window ending at the newest observation.
     *
     * Values are looked up by exact timestamp; hours without data become null.
     * Every series gets the same timestamps, so they can be combined
     * element-wise.
     */
    public static Map<String, List<SeriesPoint>> alignSeries(
            List<ObservationSeries> input, int hours, int step) {
        long maxTime = newestSeriesTime(input);
        if (maxTime == Long.MIN_VALUE) {
            return Collections.emptyMap();
        }

        long start = maxTime - (long) (hours - 1) * step;
        Map<String, List<SeriesPoint>> aligned = new LinkedHashMap<>();
        for (ObservationSeries item : input) {
            Map<Long, Double> byTime = new HashMap<>();
            for (Point point : item.getPoints()) {
                byTime.put(point.getT(), point.getV());
            }

            List<SeriesPoint> points = new ArrayList<>();
            for (long t = start; t <= maxTime; t += step) {
                Double value = byTime.get(t);
                points.add(new SeriesPoint(t,
                        value == null || value.isNaN() ? null : value));
            }
            aligned.put(item.getKey(), points);
        }
        return aligned;
    }

    public static Map<String, List<SeriesPoint>> alignSeries(List<ObservationSeries> input) {
        return alignSeries(input, DEFAULT_SERIES_HOURS, DEFAULT_SERIES_STEP);
    }

    /**
     * Takes the element-wise maximum across the given series.
     *
     * The first present series determines the output length and timestamps.
     * Null values are ignored; a position with no values stays null.
     */
    public static List<SeriesPoint> combineMax(
            Map<String, List<SeriesPoint>> series, String... keys) {
        List<SeriesPoint> first = null;
        for (String key : keys) {
            List<SeriesPoint> values = series.get(key);
            if (values != null) {
                first = values;
                break;
            }
        }
        if (first == null) {
            return Collections.emptyList();
        }

        List<SeriesPoint> combined = new ArrayList<>(first.size());
        for (int index = 0; index < first.size(); index++) {
            Double max = null;
            for (String key : keys) {
                List<SeriesPoint> values = series.get(key);
                if (values == null || index >= values.size()) {
                    continue;
                }
                Double value = values.get(index).getV();
                if (value == null || value.isNaN()) {
                    continue;
                }
                max = max == null ? value : Math.max(max, value);
            }
            combined.add(new SeriesPoint(first.get(index).getT(), max));
        }
        return combined;
    }
}
