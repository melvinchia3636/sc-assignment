package com.groupone.assignment.scrapers.parsers;

import com.groupone.assignment.repositories.model.ObservationSeries;
import com.groupone.assignment.repositories.model.Point;
import com.groupone.assignment.repositories.model.SeriesPoint;
import com.groupone.assignment.repositories.model.StationDetail;
import com.groupone.assignment.scrapers.lib.Series;
import com.groupone.assignment.scrapers.lib.Time;
import com.groupone.assignment.repositories.remote.Api2Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses numeric-station API2 feeds into station reports.
 */
public class Api2Parser {

    /** Pollutants combined into the derived AQI series, in priority order. */
    private static final String[] AQI_SERIES_KEYS = {
            "pm25", "pm10", "o3", "no2", "so2", "co",
    };

    /**
     * Parses an API2 feed response into a station report.
     *
     * Uses the first observation message that has both current readings
     * ({@code iaqi}) and a historical series ({@code obs}).
     */
    public StationDetail parse(Api2Repository.Response root, String idx) {
        Api2Repository.Message message = findObservationMessage(root);
        if (message == null || message.iaqi == null || message.obs == null) {
            return null;
        }

        List<ObservationSeries> decoded = decodeObservations(
                message.obs, message.time == null ? null : message.time.timezone);
        Map<String, List<SeriesPoint>> aligned = Series.alignSeries(decoded);

        return new StationDetail(
                idx,
                message.city == null || message.city.name == null ? "" : message.city.name,
                message.aqi == null ? Double.NaN : message.aqi,
                resolveUpdatedAt(message, decoded),
                currentValues(message),
                withAqiSeries(aligned));
    }

    /**
     * Decodes an API2 delta-encoded series into timestamped points.
     *
     * The first {@code v} entry is the latest value; subsequent entries are
     * {@code [change, seconds]} deltas going back in time, so the result is
     * reversed into ascending order. Values are divided by the series scale
     * {@code m}.
     */
    public List<Point> decodeSeries(Api2Repository.Series series, String timezone) {
        double scale = series.scale == null ? 1 : series.scale;
        List<Object> values = series.values;
        if (values == null || values.isEmpty() || !(values.get(0) instanceof Number)) {
            return Collections.emptyList();
        }

        double value = ((Number) values.get(0)).doubleValue();
        long time = Time.epochSeconds(series.start, timezone);
        List<Point> points = new ArrayList<>();
        points.add(new Point(time, value / scale));

        for (int index = 1; index < values.size(); index++) {
            Object delta = values.get(index);
            if (!(delta instanceof List)) {
                continue;
            }
            List<?> pair = (List<?>) delta;
            double change = 0;
            double seconds = 0;
            if (!pair.isEmpty() && pair.get(0) instanceof Number) {
                change = ((Number) pair.get(0)).doubleValue();
            }
            if (pair.size() > 1 && pair.get(1) instanceof Number) {
                seconds = ((Number) pair.get(1)).doubleValue();
            }
            value += change;
            time += (long) seconds;
            points.add(new Point(time, value / scale));
        }

        Collections.reverse(points);
        return points;
    }

    private Map<String, Double> currentValues(Api2Repository.Message message) {
        Map<String, Double> current = new LinkedHashMap<>();
        if (message.aqi != null && Double.isFinite(message.aqi)) {
            current.put("aqi", message.aqi);
        }
        if (message.iaqi != null) {
            for (Map.Entry<String, Api2Repository.Reading> entry : message.iaqi.entrySet()) {
                Double value = entry.getValue() == null ? null : entry.getValue().value;
                if (value != null && Double.isFinite(value)) {
                    current.put(entry.getKey(), value);
                }
            }
        }
        return current;
    }

    private Map<String, List<SeriesPoint>> withAqiSeries(
            Map<String, List<SeriesPoint>> series) {
        if (series.containsKey("aqi")) {
            return series;
        }

        List<SeriesPoint> aqi = Series.combineMax(series, AQI_SERIES_KEYS);
        if (aqi.isEmpty()) {
            return series;
        }

        Map<String, List<SeriesPoint>> withAqi = new LinkedHashMap<>();
        withAqi.put("aqi", aqi);
        withAqi.putAll(series);
        return withAqi;
    }

    private long resolveUpdatedAt(Api2Repository.Message message,
                                  List<ObservationSeries> decoded) {
        if (message.time != null && message.time.iso != null && !message.time.iso.isEmpty()) {
            return Time.epochSeconds(message.time.iso);
        }
        long timestamp = message.time == null
                ? 0
                : Time.epochSeconds(message.time.timestamp, message.time.timezone);
        return timestamp != 0
                ? timestamp
                : Math.max(0, Series.newestSeriesTime(decoded));
    }

    private Api2Repository.Message findObservationMessage(Api2Repository.Response root) {
        if (root.rxs == null || root.rxs.obs == null) {
            return null;
        }
        for (Api2Repository.Observation entry : root.rxs.obs) {
            if (entry.msg != null && entry.msg.iaqi != null && entry.msg.obs != null) {
                return entry.msg;
            }
        }
        return null;
    }

    private List<ObservationSeries> decodeObservations(
            Map<String, Api2Repository.Series> observations, String timezone) {
        List<ObservationSeries> decoded = new ArrayList<>();
        for (Map.Entry<String, Api2Repository.Series> entry : observations.entrySet()) {
            decoded.add(new ObservationSeries(entry.getKey(),
                    decodeSeries(entry.getValue(), timezone)));
        }
        return decoded;
    }
}
