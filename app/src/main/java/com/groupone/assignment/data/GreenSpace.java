package com.groupone.assignment.data;

/**
 * A park or garden returned by the OpenStreetMap Overpass API.
 */
public final class GreenSpace {

    private final String name;
    private final String type;
    private final double distanceMeters;
    private final double latitude;
    private final double longitude;

    public GreenSpace(String name, String type, double distanceMeters,
                      double latitude, double longitude) {
        this.name = name;
        this.type = type;
        this.distanceMeters = distanceMeters;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
