package dev.melvinchia3636.airngreen.scrapers.wire;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Wire shapes for the numeric WAQI api2 endpoints.
 */
public final class Api2Wire {

    private Api2Wire() {
    }

    public static final class Response {
        public Rxs rxs;
    }

    public static final class Rxs {
        public List<Observation> obs;
    }

    public static final class Observation {
        public Message msg;
    }

    public static final class Message {
        public Double aqi;
        public City city;
        public Time time;
        public Map<String, Reading> iaqi;
        public Map<String, Series> obs;
        public String token;
    }

    public static final class City {
        public String name;
    }

    public static final class Time {
        public String iso;
        @SerializedName("s")
        public String timestamp;
        @SerializedName("tz")
        public String timezone;
    }

    public static final class Reading {
        @SerializedName("v")
        public Double value;
    }

    public static final class Series {
        @SerializedName("s")
        public String start;
        @SerializedName("m")
        public Double scale;
        @SerializedName("v")
        public List<Object> values;
    }

    public static final class TokenResponse {
        public Rxs rxs;
    }
}
