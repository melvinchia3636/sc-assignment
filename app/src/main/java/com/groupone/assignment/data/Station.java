package com.groupone.assignment.data;

/**
 * A WAQI monitoring station as returned by the map bounds endpoint.
 */
public final class Station {

    private final String idx;
    private final String name;
    private final double latitude;
    private final double longitude;
    private final double aqi;
    private final String time;
    private final double distanceMeters;

    public Station(String idx, String name, double latitude, double longitude,
                   double aqi, String time, double distanceMeters) {
        this.idx = idx;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.aqi = aqi;
        this.time = time;
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

    public double getDistanceMeters() {
        return distanceMeters;
    }
}
