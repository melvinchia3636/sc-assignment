package com.groupone.assignment.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.groupone.assignment.data.StationDetail;

import org.junit.jupiter.api.Test;

class AirQualityApiTest {

    @Test
    void parse_readsStationCurrentAndSeries() {
        String json = """
                {
                  "station": {
                    "idx": "2609",
                    "name": "Kapit, Sarawak, Malaysia",
                    "aqi": 33,
                    "start": 1794880800,
                    "step": 3600,
                    "updatedAt": 1794888000,
                    "current": {"aqi": 33, "pm25": 33, "pm10": 21},
                    "series": {
                      "aqi": [30, null, 33],
                      "pm25": [30, null, 33]
                    }
                  },
                  "fetchedAt": 1794888100000
                }
                """;

        StationDetail detail = AirQualityApi.parse(json);

        assertEquals("2609", detail.getIdx());
        assertEquals("Kapit, Sarawak, Malaysia", detail.getName());
        assertEquals(33, detail.getAqi());
        assertEquals(33, detail.current("pm25"));
        assertEquals(1_794_888_000L, detail.getUpdatedAt());
        assertEquals(1_794_880_800L, detail.getStart());
        assertEquals(3, detail.series("aqi").size());
        assertTrue(detail.series("aqi").get(1).isNaN());
    }

    @Test
    void parse_missingAqi_becomesNaN() {
        StationDetail detail = AirQualityApi.parse("""
                {"station": {"idx": "A1", "name": "Unknown", "aqi": null,
                  "current": {}, "series": {}}}
                """);

        assertTrue(Double.isNaN(detail.getAqi()));
        assertTrue(detail.series("aqi").isEmpty());
    }
}
