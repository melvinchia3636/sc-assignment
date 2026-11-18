package dev.melvinchia3636.airngreen.repositories;

import android.content.Context;

import dev.melvinchia3636.airngreen.repositories.model.Bounds;
import dev.melvinchia3636.airngreen.repositories.model.Coordinates;
import dev.melvinchia3636.airngreen.repositories.model.Station;
import dev.melvinchia3636.airngreen.repositories.model.StationDetail;
import dev.melvinchia3636.airngreen.scrapers.WaqiClient;
import dev.melvinchia3636.airngreen.scrapers.lib.ApiException;

import java.util.List;

/**
 * Coordinates the WAQI station scraper and the offline cache.
 */
public class AirQualityRepository {

    private static final double MAP_RADIUS_DEGREES = 0.5;

    private final WaqiClient waqiClient;
    private final AirQualityCache cache;

    public AirQualityRepository(Context context) {
        this(new WaqiClient(), new AirQualityCache(context));
    }

    public AirQualityRepository(WaqiClient waqiClient, AirQualityCache cache) {
        this.waqiClient = waqiClient;
        this.cache = cache;
    }

    public StationDetail refresh(double latitude, double longitude) throws ApiException {
        StationDetail station = waqiClient.findNearest(new Coordinates(latitude, longitude));
        cache.save(station);
        return station;
    }

    public StationDetail loadCached() {
        return cache.load();
    }

    public List<Station> nearbyStations(double latitude, double longitude) throws ApiException {
        double radius = MAP_RADIUS_DEGREES;
        Bounds bounds = new Bounds(
                latitude - radius, longitude - radius,
                latitude + radius, longitude + radius);
        return waqiClient.fetchStations(bounds, new Coordinates(latitude, longitude));
    }
}
