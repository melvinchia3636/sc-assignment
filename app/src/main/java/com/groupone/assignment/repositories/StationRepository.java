package com.groupone.assignment.repositories;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Value types shared by the station scrapers and the UI.
 */
public final class StationRepository {

    private StationRepository() {
    }

    /** A geographic point in decimal degrees. */
    public static final class Coordinates {

        private final double lat;
        private final double lon;

        public Coordinates(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        public double getLat() {
            return lat;
        }

        public double getLon() {
            return lon;
        }
    }

    /** A geographic rectangle in decimal degrees. */
    public static final class Bounds {

        private final double south;
        private final double west;
        private final double north;
        private final double east;

        public Bounds(double south, double west, double north, double east) {
            this.south = south;
            this.west = west;
            this.north = north;
            this.east = east;
        }

        public double getSouth() {
            return south;
        }

        public double getWest() {
            return west;
        }

        public double getNorth() {
            return north;
        }

        public double getEast() {
            return east;
        }
    }

    /** One observation in a station series. */
    public static final class Point {

        private final long t;
        private final double v;

        public Point(long t, double v) {
            this.t = t;
            this.v = v;
        }

        public long getT() {
            return t;
        }

        public double getV() {
            return v;
        }
    }

    /** One point in an API series. A null value means the hour is missing. */
    public static final class SeriesPoint {

        private final long t;
        private final Double v;

        public SeriesPoint(long t, Double v) {
            this.t = t;
            this.v = v;
        }

        public long getT() {
            return t;
        }

        public Double getV() {
            return v;
        }

        public boolean isMissing() {
            return v == null || v.isNaN();
        }
    }

    /** A named observation series as returned by an upstream station. */
    public static final class ObservationSeries {

        private final String key;
        private final List<Point> points;

        public ObservationSeries(String key, List<Point> points) {
            this.key = key;
            this.points = Collections.unmodifiableList(points);
        }

        public String getKey() {
            return key;
        }

        public List<Point> getPoints() {
            return points;
        }
    }

    /** Lightweight station entry used by station lists. */
    public static final class Station {

        private final String idx;
        private final String name;
        private final double latitude;
        private final double longitude;
        private final double aqi;
        private final String time;
        private final Double distanceMeters;

        public Station(String idx, String name, double latitude, double longitude,
                       double aqi, String time, Double distanceMeters) {
            this.idx = idx == null ? "" : idx;
            this.name = name == null ? "" : name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.aqi = aqi;
            this.time = time == null ? "" : time;
            this.distanceMeters = distanceMeters;
        }

        public String getIdx() {
            return idx;
        }

        public String getName() {
            return name;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public double getAqi() {
            return aqi;
        }

        public String getTime() {
            return time;
        }

        public Double getDistanceMeters() {
            return distanceMeters;
        }
    }

    /** Full station report: current readings plus an hourly series. */
    public static final class StationDetail {

        private final String idx;
        private final String name;
        private final double aqi;
        private final long updatedAt;
        private final Map<String, Double> current;
        private final Map<String, List<SeriesPoint>> series;

        public StationDetail(String idx, String name, double aqi, long updatedAt,
                             Map<String, Double> current,
                             Map<String, List<SeriesPoint>> series) {
            this.idx = idx == null ? "" : idx;
            this.name = name == null ? "" : name;
            this.aqi = aqi;
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

        public long getUpdatedAt() {
            return updatedAt;
        }

        public Map<String, Double> getCurrent() {
            return current;
        }

        public Map<String, List<SeriesPoint>> getSeries() {
            return series;
        }

        public double current(String key) {
            Double value = current.get(key);
            return value == null ? Double.NaN : value;
        }

        public List<SeriesPoint> series(String key) {
            List<SeriesPoint> points = series.get(key);
            return points == null ? Collections.emptyList() : points;
        }

        /**
         * Series values with missing hours represented as {@link Double#NaN},
         * suitable for charts and cards.
         */
        public List<Double> values(String key) {
            List<SeriesPoint> points = series(key);
            List<Double> values = new java.util.ArrayList<>(points.size());
            for (SeriesPoint point : points) {
                values.add(point.isMissing() ? Double.NaN : point.getV());
            }
            return values;
        }
    }
}
