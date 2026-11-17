package com.groupone.assignment.repositories;

import java.util.List;
import java.util.Map;

/**
 * Wire shapes for the numeric WAQI api2 endpoints.
 */
public final class Api2Repository {

    private Api2Repository() {
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
        public String s;
        public String tz;
    }

    public static final class Reading {
        public Double v;
    }

    public static final class Series {
        public String s;
        public Double m;
        public List<Object> v;
    }

    public static final class TokenResponse {
        public Rxs rxs;
    }
}
