package dev.melvinchia3636.airngreen.repositories.model;

/** One point in an API series. A null value means the hour is missing. */
public final class SeriesPoint {

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
