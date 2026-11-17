package com.groupone.assignment.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.groupone.assignment.repositories.model.SeriesPoint;
import com.groupone.assignment.repositories.model.StationDetail;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class StationDetailSerializationTest {

    private static final Gson GSON = new GsonBuilder()
            .serializeSpecialFloatingPointValues()
            .create();

    @Test
    void gsonRoundTrip_preservesStationAndMissingValues() {
        Map<String, Double> current = new LinkedHashMap<>();
        current.put("pm25", 56.0);
        current.put("o3", Double.NaN);

        Map<String, List<SeriesPoint>> series = new LinkedHashMap<>();
        series.put("aqi", List.of(
                new SeriesPoint(1_700_000_000L, 39.0),
                new SeriesPoint(1_700_003_600L, null),
                new SeriesPoint(1_700_007_200L, 56.0)));

        StationDetail original = new StationDetail("5780", "Kuala Lumpur",
                56.0, 1_700_000_100L, current, series);

        StationDetail restored = GSON.fromJson(GSON.toJson(original), StationDetail.class);

        assertEquals("5780", restored.getIdx());
        assertEquals("Kuala Lumpur", restored.getName());
        assertEquals(56.0, restored.getAqi());
        assertEquals(56.0, restored.current("pm25"));
        assertTrue(Double.isNaN(restored.current("o3")));
        assertEquals(3, restored.series("aqi").size());
        assertTrue(restored.series("aqi").get(1).isMissing());
        assertEquals(1_700_003_600L, restored.series("aqi").get(1).getT());
    }
}
