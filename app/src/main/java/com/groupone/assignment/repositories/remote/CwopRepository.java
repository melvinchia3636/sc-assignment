package com.groupone.assignment.repositories.remote;

import com.google.gson.annotations.SerializedName;

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
        @SerializedName("h")
        public Double humidity;
        /** Barometric pressure, in hPa. */
        @SerializedName("p")
        public Double pressure;
        /** Air temperature, in degrees Celsius. */
        @SerializedName("t")
        public Double temperature;
        /** Wind speed, in the provider's reported unit. */
        @SerializedName("ws")
        public Double windSpeed;
    }
}
