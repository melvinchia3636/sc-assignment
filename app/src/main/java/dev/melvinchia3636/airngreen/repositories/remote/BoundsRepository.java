package dev.melvinchia3636.airngreen.repositories.remote;

import java.util.List;

/**
 * Wire shape for the WAQI map bounds endpoint.
 */
public final class BoundsRepository {

    private BoundsRepository() {
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
