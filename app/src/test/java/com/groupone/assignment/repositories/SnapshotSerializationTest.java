package com.groupone.assignment.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.groupone.assignment.repositories.StationRepository.SeriesPoint;
import com.groupone.assignment.repositories.StationRepository.StationDetail;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class SnapshotSerializationTest {

    private static final Gson GSON = new GsonBuilder()
            .serializeSpecialFloatingPointValues()
            .create();

    @Test
    void gsonRoundTrip_preservesSnapshotAndMissingValues() {
        Map<String, Double> current = new LinkedHashMap<>();
        current.put("pm25", 56.0);
        current.put("o3", Double.NaN);

        Map<String, List<SeriesPoint>> series = new LinkedHashMap<>();
        series.put("aqi", List.of(
                new SeriesPoint(1_700_000_000L, 39.0),
                new SeriesPoint(1_700_003_600L, null),
                new SeriesPoint(1_700_007_200L, 56.0)));

        StationDetail station = new StationDetail("5780", "Kuala Lumpur",
                56.0, 1_700_000_100L, current, series);
        Snapshot original = new Snapshot(station, 1_700_000_200L);

        Snapshot restored = GSON.fromJson(GSON.toJson(original), Snapshot.class);

        assertEquals("5780", restored.getStationDetail().getIdx());
        assertEquals("Kuala Lumpur", restored.getStationDetail().getName());
        assertEquals(56.0, restored.getStationDetail().getAqi());
        assertEquals(56.0, restored.getStationDetail().current("pm25"));
        assertTrue(Double.isNaN(restored.getStationDetail().current("o3")));
        assertEquals(3, restored.getStationDetail().series("aqi").size());
        assertTrue(restored.getStationDetail().series("aqi").get(1).isMissing());
        assertEquals(1_700_003_600L, restored.getStationDetail().series("aqi").get(1).getT());
        assertEquals(1_700_000_200L, restored.getSavedAtMillis());
    }
}
