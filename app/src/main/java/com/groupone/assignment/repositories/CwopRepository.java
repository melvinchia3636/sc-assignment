package com.groupone.assignment.repositories;

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
        public Double h;
        public Double p;
        public Double t;
        public Double ws;
    }
}
