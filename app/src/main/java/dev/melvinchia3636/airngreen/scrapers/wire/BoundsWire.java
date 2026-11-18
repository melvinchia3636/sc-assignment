package dev.melvinchia3636.airngreen.scrapers.wire;

import java.util.List;

/**
 * Wire shape for the WAQI map bounds endpoint.
 */
public final class BoundsWire {

    private BoundsWire() {
    }

    public static final class Response {
        public List<Entry> data;
    }

    public static final class Entry {
        public String idx;
        public String name;
        public Object aqi;
        public List<Double> geo;
        public String utime;
    }
}
