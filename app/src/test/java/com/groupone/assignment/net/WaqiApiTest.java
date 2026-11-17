package com.groupone.assignment.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.groupone.assignment.data.Station;
import com.groupone.assignment.data.StationDetail;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class WaqiApiTest {

    @Test
    void pm25ToAqi_matchesEpaBreakpoints() {
        assertEquals(0, WaqiApi.pm25ToAqi(0));
        assertEquals(50, WaqiApi.pm25ToAqi(12.0));
        assertEquals(100, WaqiApi.pm25ToAqi(35.4));
        assertEquals(150, WaqiApi.pm25ToAqi(55.4));
        assertEquals(300, WaqiApi.pm25ToAqi(250.4));
        assertEquals(500, WaqiApi.pm25ToAqi(600));
    }

    @Test
    void pm10ToAqi_matchesEpaBreakpoints() {
        assertEquals(50, WaqiApi.pm10ToAqi(54));
        assertEquals(100, WaqiApi.pm10ToAqi(154));
        assertEquals(300, WaqiApi.pm10ToAqi(424));
        assertEquals(500, WaqiApi.pm10ToAqi(700));
    }

    @Test
    void decodeApi2Series_expandsDeltasAndReverses() throws Exception {
        JSONObject series = new JSONObject("""
                {"s": "2026-11-17 10:00:00", "m": 1, "v": [33, [9, -3600], [-13, -3600]]}
                """);

        List<WaqiApi.Point> points = WaqiApi.decodeApi2Series(series, "+08:00");

        assertEquals(3, points.size());
        assertEquals(29, points.get(0).v);
        assertEquals(42, points.get(1).v);
        assertEquals(33, points.get(2).v);
        assertEquals(3600, points.get(1).t - points.get(0).t);
    }

    @Test
    void alignSeries_padsMissingHoursWithNaN() {
        List<WaqiApi.Series> input = new ArrayList<>();
        long now = 1_000_000;
        input.add(new WaqiApi.Series("pm25", List.of(
                new WaqiApi.Point(now - 7200, 10),
                new WaqiApi.Point(now, 30))));

        WaqiApi.Aligned aligned = WaqiApi.alignSeries(input, 3, 3600);

        List<Double> values = aligned.series.get("pm25");
        assertEquals(3, values.size());
        assertEquals(10, values.get(0));
        assertTrue(values.get(1).isNaN());
        assertEquals(30, values.get(2));
    }

    @Test
    void parseStations_sortsByDistanceAndHandlesMissingAqi() throws Exception {
        JSONObject root = new JSONObject("""
                {"data": [
                  {"idx": "A2", "name": "Far", "aqi": "66", "geo": [3.30, 101.70]},
                  {"idx": "A1", "name": "Near", "aqi": "-", "geo": [3.140, 101.687]},
                  {"idx": "A3", "name": "Invalid", "aqi": "10"}
                ]}
                """);

        List<Station> stations = WaqiApi.parseStations(root, 3.139, 101.6869);

        assertEquals(2, stations.size());
        assertEquals("Near", stations.get(0).getName());
        assertTrue(Double.isNaN(stations.get(0).getAqi()));
        assertEquals("Far", stations.get(1).getName());
        assertEquals(66, stations.get(1).getAqi());
    }

    @Test
    void parseApi2Detail_readsCurrentSeriesAndAqi() throws Exception {
        JSONObject root = new JSONObject("""
                {"rxs": {"obs": [{"msg": {
                  "aqi": 33,
                  "city": {"name": "Kapit, Sarawak, Malaysia"},
                  "time": {"iso": "2026-11-17T10:00:00+08:00", "tz": "+08:00", "v": 1794909600},
                  "iaqi": {"pm25": {"v": 33}, "pm10": {"v": 21}},
                  "obs": {
                    "pm25": {"s": "2026-11-17 10:00:00", "m": 1, "v": [33, [9, -3600]]}
                  }
                }}]}}
                """);

        StationDetail detail = WaqiApi.parseApi2Detail(root, "2609");

        assertNotNull(detail);
        assertEquals("2609", detail.getIdx());
        assertEquals("Kapit, Sarawak, Malaysia", detail.getName());
        assertEquals(33, detail.getAqi());
        assertEquals(33, detail.current("pm25"));
        assertEquals(1794909600, detail.getUpdatedAt());
        assertFalse(detail.series("aqi").isEmpty());
    }

    @Test
    void parseAirnetDetail_readsFeedWeatherAndAlignedSeries() throws Exception {
        JSONObject hourly = new JSONObject("""
                {
                  "meta": {"id": "2820325", "name": "Bukit Gita Bayu", "utime": 1794885398},
                  "feed": {"pm25": [1794885398, 2148], "pm10": [1794885398, 2294]},
                  "data": {
                    "pm25": [
                      {"time": "2026-11-17T02:00:00Z", "mean": 37.27},
                      {"time": "2026-11-17T03:00:00Z", "mean": 21.6}
                    ],
                    "pm10": [
                      {"time": "2026-11-17T02:00:00Z", "mean": 46.06},
                      {"time": "2026-11-17T03:00:00Z", "mean": 24.15}
                    ]
                  }
                }
                """);
        JSONObject cwop = new JSONObject("""
                {"data": {"data": {
                  "2026-11-17T03:00:00Z": {"h": 70.5, "p": 1010.5, "t": 31.6, "ws": 7}
                }}}
                """);

        StationDetail detail = WaqiApi.parseAirnetDetail(hourly, cwop, "A2820325");

        assertNotNull(detail);
        assertEquals("A2820325", detail.getIdx());
        assertEquals("Bukit Gita Bayu", detail.getName());
        assertEquals(71, detail.getAqi());
        assertEquals(71, detail.current("pm25"));
        assertEquals(31.6, detail.current("t"));
        assertEquals(72, detail.series("aqi").size());
        assertEquals(71, detail.series("aqi").get(71));
    }

    @Test
    void epochSeconds_appliesTimezoneOffset() {
        long utc = WaqiApi.epochSeconds("2026-11-17T02:00:00Z", "+00:00");
        long local = WaqiApi.epochSeconds("2026-11-17 10:00:00", "+08:00");

        assertEquals(utc, local);
    }
}
