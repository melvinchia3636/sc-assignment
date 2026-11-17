package com.groupone.assignment.scrapers.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.groupone.assignment.repositories.model.ObservationSeries;
import com.groupone.assignment.repositories.model.Point;
import com.groupone.assignment.repositories.model.SeriesPoint;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class SeriesTest {

    @Test
    void alignSeries_padsMissingHoursWithNull() {
        long now = 1_000_000;
        ObservationSeries input = new ObservationSeries("pm25", List.of(
                new Point(now - 7200, 10),
                new Point(now, 30)));

        Map<String, List<SeriesPoint>> aligned = Series.alignSeries(
                List.of(input), 3, 3600);

        List<SeriesPoint> points = aligned.get("pm25");
        assertEquals(3, points.size());
        assertEquals(now - 7200, points.get(0).getT());
        assertEquals(10, points.get(0).getV());
        assertNull(points.get(1).getV());
        assertEquals(now, points.get(2).getT());
        assertEquals(30, points.get(2).getV());
    }

    @Test
    void combineMax_prefersTheHighestAvailableSubIndex() {
        Map<String, List<SeriesPoint>> series = new LinkedHashMap<>();
        series.put("pm25", List.of(
                new SeriesPoint(0, 10.0),
                new SeriesPoint(1, null),
                new SeriesPoint(2, 30.0)));
        series.put("pm10", List.of(
                new SeriesPoint(0, 20.0),
                new SeriesPoint(1, 40.0),
                new SeriesPoint(2, null)));

        List<SeriesPoint> combined = Series.combineMax(series, "pm25", "pm10");

        assertEquals(3, combined.size());
        assertEquals(20, combined.get(0).getV());
        assertEquals(40, combined.get(1).getV());
        assertEquals(30, combined.get(2).getV());
    }
}
