package com.groupone.assignment.scrapers.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.groupone.assignment.repositories.model.StationRepository.Coordinates;
import com.groupone.assignment.repositories.model.StationRepository.Point;
import com.groupone.assignment.repositories.model.StationRepository.Station;
import com.groupone.assignment.repositories.model.StationRepository.StationDetail;
import com.groupone.assignment.repositories.remote.AirnetRepository;
import com.groupone.assignment.repositories.remote.Api2Repository;
import com.groupone.assignment.repositories.remote.BoundsRepository;
import com.groupone.assignment.repositories.remote.CwopRepository;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class ParsersTest {

    private final Api2Parser api2Parser = new Api2Parser();
    private final AirnetParser airnetParser = new AirnetParser();
    private final StationsParser stationsParser = new StationsParser();

    @Test
    void pm25_matchesTheEpaTable() {
        assertEquals(0, AirnetParser.pm25ToAqi(0));
        assertEquals(50, AirnetParser.pm25ToAqi(12.0));
        assertEquals(100, AirnetParser.pm25ToAqi(35.4));
        assertEquals(150, AirnetParser.pm25ToAqi(55.4));
        assertEquals(300, AirnetParser.pm25ToAqi(250.4));
        assertEquals(500, AirnetParser.pm25ToAqi(600));
    }

    @Test
    void pm10_matchesTheEpaTable() {
        assertEquals(50, AirnetParser.pm10ToAqi(54));
        assertEquals(100, AirnetParser.pm10ToAqi(154));
        assertEquals(300, AirnetParser.pm10ToAqi(424));
        assertEquals(500, AirnetParser.pm10ToAqi(700));
    }

    @Test
    void stations_sortsByDistanceAndHandlesMissingAqi() {
        BoundsRepository.Response root = new BoundsRepository.Response();
        root.data = List.of(
                boundsEntry("A2", "Far", "66", 3.3, 101.7),
                boundsEntry("A1", "Near", "-", 3.14, 101.687),
                boundsEntry("A3", "Invalid", "10", null, null));

        List<Station> stations = stationsParser.parse(
                root, new Coordinates(3.139, 101.6869));

        assertEquals(2, stations.size());
        assertEquals("Near", stations.get(0).getName());
        assertTrue(Double.isNaN(stations.get(0).getAqi()));
        assertEquals("Far", stations.get(1).getName());
        assertEquals(66, stations.get(1).getAqi());
    }

    @Test
    void stations_cleansAndSortsTheGlobalListWithoutDistances() {
        BoundsRepository.Response root = new BoundsRepository.Response();
        root.data = List.of(
                boundsEntry("B", "Beta", null, 1.0, 2.0),
                boundsEntry("A", "Alpha", null, 3.0, 4.0),
                boundsEntry("", "No id", null, 5.0, 6.0),
                boundsEntry("C", "No geo", null, null, null),
                boundsEntry("D", null, null, 7.0, 8.0));

        List<Station> stations = stationsParser.parse(root, null);

        assertEquals(3, stations.size());
        assertEquals("A", stations.get(0).getIdx());
        assertEquals("B", stations.get(1).getIdx());
        assertEquals("D", stations.get(2).getIdx());
        assertEquals("D", stations.get(2).getName());
        assertNull(stations.get(0).getDistanceMeters());
    }

    @Test
    void api2_expandsDeltasAndReversesTheSeries() {
        Api2Repository.Series series = new Api2Repository.Series();
        series.start = "2026-11-17 10:00:00";
        series.scale = 1.0;
        series.values = List.of(33.0, List.of(9.0, -3600.0), List.of(-13.0, -3600.0));

        List<Point> points = api2Parser.decodeSeries(series, "+08:00");

        assertEquals(3, points.size());
        assertEquals(29, points.get(0).getV());
        assertEquals(42, points.get(1).getV());
        assertEquals(33, points.get(2).getV());
        assertEquals(3600, points.get(1).getT() - points.get(0).getT());
    }

    @Test
    void api2_readsCurrentSeriesAndAqi() {
        Api2Repository.Series pm25 = new Api2Repository.Series();
        pm25.start = "2026-11-17 10:00:00";
        pm25.scale = 1.0;
        pm25.values = List.of(33.0, List.of(9.0, -3600.0));

        Api2Repository.Message message = new Api2Repository.Message();
        message.aqi = 33.0;
        message.city = new Api2Repository.City();
        message.city.name = "Kapit, Sarawak, Malaysia";
        message.time = new Api2Repository.Time();
        message.time.iso = "2026-11-17T10:00:00+08:00";
        message.time.timezone = "+08:00";
        message.iaqi = Map.of("pm25", reading(33), "pm10", reading(21));
        message.obs = Map.of("pm25", pm25);

        Api2Repository.Observation observation = new Api2Repository.Observation();
        observation.msg = message;
        Api2Repository.Rxs rxs = new Api2Repository.Rxs();
        rxs.obs = List.of(observation);
        Api2Repository.Response root = new Api2Repository.Response();
        root.rxs = rxs;

        StationDetail detail = api2Parser.parse(root, "2609");

        assertNotNull(detail);
        assertEquals("2609", detail.getIdx());
        assertEquals("Kapit, Sarawak, Malaysia", detail.getName());
        assertEquals(33, detail.getAqi());
        assertEquals(33, detail.current("pm25"));
        assertEquals(1_794_880_800L, detail.getUpdatedAt());
        assertTrue(detail.series("aqi").size() > 0);
    }

    @Test
    void airnet_readsFeedWeatherAndAlignedSeries() {
        AirnetRepository.Response hourly = new AirnetRepository.Response();
        hourly.meta = new AirnetRepository.Meta();
        hourly.meta.name = "Bukit Gita Bayu";
        hourly.meta.utime = 1794885398L;

        Map<String, List<Double>> feed = new LinkedHashMap<>();
        feed.put("pm25", List.of(1794885398.0, 2148.0));
        feed.put("pm10", List.of(1794885398.0, 2294.0));
        hourly.feed = feed;

        hourly.data = new AirnetRepository.Data();
        hourly.data.pm25 = List.of(
                airnetEntry("2026-11-17T02:00:00Z", 37.27),
                airnetEntry("2026-11-17T03:00:00Z", 21.6));
        hourly.data.pm10 = List.of(
                airnetEntry("2026-11-17T02:00:00Z", 46.06),
                airnetEntry("2026-11-17T03:00:00Z", 24.15));

        CwopRepository.Reading weather = new CwopRepository.Reading();
        weather.humidity = 70.5;
        weather.pressure = 1010.5;
        weather.temperature = 31.6;
        weather.windSpeed = 7.0;
        CwopRepository.Data weatherData = new CwopRepository.Data();
        weatherData.data = Map.of("2026-11-17T03:00:00Z", weather);
        CwopRepository.Response cwop = new CwopRepository.Response();
        cwop.data = weatherData;

        StationDetail detail = airnetParser.parse(hourly, cwop, "A2820325");

        assertNotNull(detail);
        assertEquals("A2820325", detail.getIdx());
        assertEquals("Bukit Gita Bayu", detail.getName());
        assertEquals(71, detail.getAqi());
        assertEquals(71, detail.current("pm25"));
        assertEquals(31.6, detail.current("t"), 0.0001);
        assertEquals(72, detail.series("aqi").size());
        assertEquals(71, detail.series("aqi").get(71).getV());
    }

    private static BoundsRepository.Entry boundsEntry(
            String idx, String name, String aqi, Double latitude, Double longitude) {
        BoundsRepository.Entry entry = new BoundsRepository.Entry();
        entry.idx = idx;
        entry.name = name;
        entry.aqi = aqi;
        if (latitude != null && longitude != null) {
            List<Double> geo = new ArrayList<>();
            geo.add(latitude);
            geo.add(longitude);
            entry.geo = geo;
        }
        return entry;
    }

    private static Api2Repository.Reading reading(double value) {
        Api2Repository.Reading reading = new Api2Repository.Reading();
        reading.value = value;
        return reading;
    }

    private static AirnetRepository.Entry airnetEntry(String time, double mean) {
        AirnetRepository.Entry entry = new AirnetRepository.Entry();
        entry.time = time;
        entry.mean = mean;
        return entry;
    }
}
