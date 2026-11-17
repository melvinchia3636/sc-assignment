package com.groupone.assignment.repositories.model;

/** One observation in a station series. */
public final class Point {

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
