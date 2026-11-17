package com.groupone.assignment.repositories.model;

import java.util.Collections;
import java.util.List;

/** A named observation series as returned by an upstream station. */
public final class ObservationSeries {

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
