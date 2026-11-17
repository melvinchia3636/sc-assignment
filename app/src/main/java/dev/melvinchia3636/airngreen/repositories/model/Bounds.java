package dev.melvinchia3636.airngreen.repositories.model;

/** A geographic rectangle in decimal degrees. */
public final class Bounds {

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
