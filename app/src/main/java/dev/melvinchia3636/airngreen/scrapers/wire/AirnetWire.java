package dev.melvinchia3636.airngreen.scrapers.wire;

import java.util.List;
import java.util.Map;

/**
 * Wire shapes for the AirNet hourly feed and token endpoints.
 */
public final class AirnetWire {

    private AirnetWire() {
    }

    public static final class Response {
        public Meta meta;
        public Map<String, List<Double>> feed;
        public Data data;
    }

    public static final class Meta {
        public String id;
        public String name;
        public Long utime;
    }

    public static final class Data {
        public List<Entry> pm25;
        public List<Entry> pm10;
        public List<Entry> pm1;
    }

    public static final class Entry {
        public String time;
        public Double mean;
    }

    public static final class TokenResponse {
        public String key;
    }
}
