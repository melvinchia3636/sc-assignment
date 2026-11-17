package com.groupone.assignment.data;

import android.content.Context;

import com.groupone.assignment.net.AirQualityApi;
import com.groupone.assignment.net.GreenSpaceApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Fetches the station snapshot from the aqi-api service, fetches nearby green
 * spaces from OpenStreetMap and keeps the last successful result on disk.
 */
public class AirQualityRepository {

    private static final int GREEN_SPACE_RADIUS_METERS = 3_000;

    private final AirQualityApi airQualityApi;
    private final GreenSpaceApi greenSpaceApi;
    private final SnapshotCache cache;

    public AirQualityRepository(Context context) {
        this(new AirQualityApi(), new GreenSpaceApi(), new SnapshotCache(context));
    }

    public AirQualityRepository(
            AirQualityApi airQualityApi,
            GreenSpaceApi greenSpaceApi,
            SnapshotCache cache) {
        this.airQualityApi = airQualityApi;
        this.greenSpaceApi = greenSpaceApi;
        this.cache = cache;
    }

    /**
     * Fetches the nearest reporting station. A green space failure is
     * tolerated so the air quality reading can still be shown.
     */
    public Snapshot refresh(double latitude, double longitude) throws ApiException {
        StationDetail stationDetail = airQualityApi.fetch(latitude, longitude);

        List<GreenSpace> greenSpaces = new ArrayList<>();
        boolean greenSpacesUnavailable = false;
        try {
            greenSpaces = greenSpaceApi.fetchNearby(
                    latitude, longitude, GREEN_SPACE_RADIUS_METERS);
        } catch (ApiException greenSpaceFailure) {
            greenSpacesUnavailable = true;
        }

        Snapshot snapshot = new Snapshot(
                stationDetail, greenSpaces, System.currentTimeMillis(), greenSpacesUnavailable);
        cache.save(snapshot);
        return snapshot;
    }

    public Snapshot loadCached() {
        return cache.load();
    }
}
