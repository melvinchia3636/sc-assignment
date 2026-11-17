package com.groupone.assignment.repositories;

import com.groupone.assignment.repositories.model.StationRepository.StationDetail;

/**
 * The last fetched station reading and when it was saved.
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
