package com.groupone.assignment.repositories;

import android.content.Context;

import com.groupone.assignment.repositories.local.AirQualityCache;
import com.groupone.assignment.repositories.model.StationRepository.Coordinates;
import com.groupone.assignment.repositories.model.StationRepository.StationDetail;
import com.groupone.assignment.scrapers.WaqiClient;

/**
 * Coordinates the WAQI station scraper and the offline cache.
 */
public class AirQualityRepository {

    private final WaqiClient waqiClient;
    private final AirQualityCache cache;

    public AirQualityRepository(Context context) {
        this(new WaqiClient(), new AirQualityCache(context));
    }

    public AirQualityRepository(WaqiClient waqiClient, AirQualityCache cache) {
        this.waqiClient = waqiClient;
        this.cache = cache;
    }

    public Snapshot refresh(double latitude, double longitude) throws ApiException {
        StationDetail stationDetail = waqiClient.findNearest(
                new Coordinates(latitude, longitude));

        Snapshot snapshot = new Snapshot(stationDetail, System.currentTimeMillis());
        cache.save(snapshot);
        return snapshot;
    }

    public Snapshot loadCached() {
        return cache.load();
    }
}
