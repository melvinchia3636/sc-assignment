package com.groupone.assignment.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

        Map<String, List<Double>> series = new LinkedHashMap<>();
        series.put("aqi", List.of(39.0, Double.NaN, 56.0));

        StationDetail station = new StationDetail("5780", "Kuala Lumpur",
                56.0, 1_700_000_000L, 3600, 1_700_000_100L, current, series);
        GreenSpace park = new GreenSpace("KLCC Park", "Park", 820, 3.15, 101.71);
        Snapshot original = new Snapshot(station, List.of(park), 1_700_000_200L, false);

        Snapshot restored = GSON.fromJson(GSON.toJson(original), Snapshot.class);

        assertEquals("5780", restored.getStationDetail().getIdx());
        assertEquals("Kuala Lumpur", restored.getStationDetail().getName());
        assertEquals(56.0, restored.getStationDetail().getAqi());
        assertEquals(56.0, restored.getStationDetail().current("pm25"));
        assertTrue(Double.isNaN(restored.getStationDetail().current("o3")));
        assertEquals(3, restored.getStationDetail().series("aqi").size());
        assertTrue(Double.isNaN(restored.getStationDetail().series("aqi").get(1)));
        assertEquals(1, restored.getGreenSpaces().size());
        assertEquals("KLCC Park", restored.getGreenSpaces().get(0).getName());
        assertEquals(1_700_000_200L, restored.getSavedAtMillis());
    }
}
