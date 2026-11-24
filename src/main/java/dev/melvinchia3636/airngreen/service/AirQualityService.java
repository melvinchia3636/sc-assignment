package dev.melvinchia3636.airngreen.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import dev.melvinchia3636.airngreen.api.AqiApiClient;
import dev.melvinchia3636.airngreen.api.ApiException;
import dev.melvinchia3636.airngreen.model.District;
import dev.melvinchia3636.airngreen.model.StationReading;
import dev.melvinchia3636.airngreen.model.StationSummary;
import dev.melvinchia3636.airngreen.storage.LocalStore;

/**
 * Loads air-quality data and keeps the last good result for offline use.
 *
 * <p>Every successful call updates the local cache, so a later failure can
 * still be shown to the user.</p>
 */
public final class AirQualityService {

    private static final double STATION_RADIUS_DEGREES = 0.5;

    private final AqiApiClient client;
    private final LocalStore store;

    /**
     * Creates the service.
     *
     * @param client API client
     * @param store  local cache
     */
    public AirQualityService(AqiApiClient client, LocalStore store) {
        this.client = client;
        this.store = store;
    }

    /**
     * Loads the nearest station to a district.
     *
     * @param district district to query
     * @return the current reading
     * @throws ApiException when the service cannot answer
     */
    public StationReading nearest(District district) throws ApiException {
        StationReading reading =
                client.nearestStation(district.latitude(), district.longitude());
        store.saveReading(reading);
        return reading;
    }

    /**
     * Loads one station by id.
     *
     * @param idx upstream station id
     * @return the current reading
     * @throws ApiException when the station is unknown or the service fails
     */
    public StationReading byIdx(String idx) throws ApiException {
        StationReading reading = client.station(idx);
        store.saveReading(reading);
        return reading;
    }

    /**
     * Loads the station directory plus every station near a district.
     *
     * <p>The worldwide directory from WAQI omits some city stations, so the
     * district-sized query is merged in by station id.</p>
     *
     * @param district district whose stations must be included
     * @return the merged directory, nearest station to the district first
     * @throws ApiException when either request fails
     */
    public List<StationSummary> stations(District district) throws ApiException {
        Map<String, StationSummary> byId = new LinkedHashMap<>();
        for (StationSummary station : client.stations()) {
            byId.put(station.idx(), station);
        }

        double radius = STATION_RADIUS_DEGREES;
        List<StationSummary> local = client.stations(
                district.latitude() - radius,
                district.longitude() - radius,
                district.latitude() + radius,
                district.longitude() + radius);
        for (StationSummary station : local) {
            byId.put(station.idx(), station);
        }

        List<StationSummary> merged = new ArrayList<>(byId.values());
        merged.sort(Comparator.comparingDouble(station ->
                GeoDistance.kmBetween(district.latitude(), district.longitude(),
                        station.latitude(), station.longitude())));
        store.saveStations(merged);
        return merged;
    }

    /**
     * Last successfully loaded reading, even across restarts.
     *
     * @return the cached reading, or empty when none exists
     */
    public Optional<StationReading> getCachedReading() {
        return store.loadReading();
    }

    /**
     * Cached station directory, for offline use.
     *
     * @return the cached stations, or an empty list
     */
    public List<StationSummary> getCachedStations() {
        return store.loadStations();
    }

    /**
     * Checks whether the service is reachable.
     *
     * @return true when the liveness endpoint answers
     */
    public boolean isServiceAlive() {
        return client.isAlive();
    }
}
