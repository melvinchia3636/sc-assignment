package com.groupone.assignment.repositories.remote;

import java.util.Map;

/**
 * Wire shape for the CWOP weather feed.
 */
public final class CwopRepository {

    private CwopRepository() {
    }

    public static final class Response {
        public Data data;
    }

    public static final class Data {
        public Map<String, Reading> data;
    }

    /** One CWOP weather observation. */
    public static final class Reading {
        /** Relative humidity, in percent. */
        public Double h;
        /** Barometric pressure, in hPa. */
        public Double p;
        /** Air temperature, in degrees Celsius. */
        public Double t;
        /** Wind speed, in the provider's reported unit. */
        public Double ws;
    }
}
