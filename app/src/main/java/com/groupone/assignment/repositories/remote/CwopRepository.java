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

    public static final class Reading {
        @SerializedName("h")
        public Double humidity;
        @SerializedName("p")
        public Double pressure;
        @SerializedName("t")
        public Double temperature;
        @SerializedName("ws")
        public Double windSpeed;
    }
}
