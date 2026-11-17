package com.groupone.assignment.data;

import java.util.Collections;
import java.util.List;

/**
 * A complete screen's worth of data: the nearest WAQI station reading plus
 * nearby green spaces, with the time it was fetched.
 */
public final class Snapshot {

    private final StationDetail stationDetail;
    private final List<GreenSpace> greenSpaces;
    private final long savedAtMillis;
    private final boolean greenSpacesUnavailable;

    public Snapshot(StationDetail stationDetail, List<GreenSpace> greenSpaces,
                    long savedAtMillis, boolean greenSpacesUnavailable) {
        this.stationDetail = stationDetail;
        this.greenSpaces = Collections.unmodifiableList(greenSpaces);
        this.savedAtMillis = savedAtMillis;
        this.greenSpacesUnavailable = greenSpacesUnavailable;
    }

    public StationDetail getStationDetail() {
        return stationDetail;
    }

    public List<GreenSpace> getGreenSpaces() {
        return greenSpaces;
    }

    public long getSavedAtMillis() {
        return savedAtMillis;
    }

    public boolean isGreenSpacesUnavailable() {
        return greenSpacesUnavailable;
    }
}
