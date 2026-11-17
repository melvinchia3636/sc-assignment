package dev.melvinchia3636.airngreen.repositories.model;

/** Lightweight station entry used by station lists. */
public final class Station {

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
