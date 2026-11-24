package dev.melvinchia3636.airngreen.service;

import java.util.List;
import java.util.Optional;

import dev.melvinchia3636.airngreen.api.AqiApiClient;
import dev.melvinchia3636.airngreen.api.ApiException;
import dev.melvinchia3636.airngreen.model.District;
import dev.melvinchia3636.airngreen.model.StationReading;
import dev.melvinchia3636.airngreen.model.StationSummary;

/**
 * Loads air-quality data and keeps the last good result for offline use.
 *
 * <p>Every successful call updates the local cache, so a later failure can
 * still be shown to the user.</p>
 */
public final class AirQualityService {

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
     * Loads the station directory.
     *
     * @return every station known to the service
     * @throws ApiException when the service cannot answer
     */
    public List<StationSummary> stations() throws ApiException {
        List<StationSummary> stations = client.stations();
        store.saveStations(stations);
        return stations;
    }

    /**
     * Last successfully loaded reading, even across restarts.
     *
     * @return the cached reading, or empty when none exists
     */
    public Optional<StationReading> cachedReading() {
        return store.loadReading();
    }

    /**
     * Cached station directory, for offline use.
     *
     * @return the cached stations, or an empty list
     */
    public List<StationSummary> cachedStations() {
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
