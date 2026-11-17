package com.groupone.assignment.repositories;

import com.groupone.assignment.repositories.StationRepository.StationDetail;

/**
 * A complete screen's worth of data: the nearest WAQI station reading, with
 * the time it was fetched.
 */
public final class Snapshot {

    private final StationDetail stationDetail;
    private final long savedAtMillis;

    public Snapshot(StationDetail stationDetail, long savedAtMillis) {
        this.stationDetail = stationDetail;
        this.savedAtMillis = savedAtMillis;
    }

    public StationDetail getStationDetail() {
        return stationDetail;
    }

    public long getSavedAtMillis() {
        return savedAtMillis;
    }
}
