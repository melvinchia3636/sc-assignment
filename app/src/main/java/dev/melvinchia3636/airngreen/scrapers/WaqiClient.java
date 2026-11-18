package dev.melvinchia3636.airngreen.scrapers;

import dev.melvinchia3636.airngreen.R;
import dev.melvinchia3636.airngreen.repositories.model.Bounds;
import dev.melvinchia3636.airngreen.repositories.model.Coordinates;
import dev.melvinchia3636.airngreen.repositories.model.SeriesPoint;
import dev.melvinchia3636.airngreen.repositories.model.Station;
import dev.melvinchia3636.airngreen.repositories.model.StationDetail;
import dev.melvinchia3636.airngreen.scrapers.lib.ApiException;
import dev.melvinchia3636.airngreen.scrapers.lib.TtlCache;
import dev.melvinchia3636.airngreen.scrapers.lib.Upstream;
import dev.melvinchia3636.airngreen.scrapers.parsers.StationsParser;
import dev.melvinchia3636.airngreen.scrapers.sources.AirnetStationSource;
import dev.melvinchia3636.airngreen.scrapers.sources.Api2StationSource;
import dev.melvinchia3636.airngreen.scrapers.sources.StationSource;
import dev.melvinchia3636.airngreen.scrapers.wire.BoundsWire;
import java.util.List;
import java.util.Locale;

import okhttp3.HttpUrl;

/**
 * WAQI client: discovers nearby stations from the map bounds feed and fetches
 * per-station detail through the provider-specific source.
 *
 * Station lists and details are cached in memory for {@link #STATION_TTL_MS}.
 */
public class WaqiClient {

    private static final String STATIONS_URL = "https://mapq.waqi.info/mapq2/bounds";
    private static final Bounds WORLD_BOUNDS = new Bounds(-90, -180, 90, 180);
    private static final double[] SEARCH_RADII = { 0.35, 1.0, 3.0 };
    private static final int MAX_STATION_ATTEMPTS = 3;
    private static final long STATION_TTL_MS = 5 * 60 * 1000;

    private final StationSource api2 = new Api2StationSource();
    private final StationSource airnet = new AirnetStationSource();
    private final StationsParser stationParser = new StationsParser();
    private final TtlCache<List<Station>> stationLists = new TtlCache<>();
    private final TtlCache<StationDetail> stationDetails = new TtlCache<>();

    /**
     * Finds the nearest station that actually reports readings.
     *
     * Searches outward through the search radii, trying up to
     * {@link #MAX_STATION_ATTEMPTS} stations per radius.
     */
    public StationDetail findNearest(Coordinates origin) throws ApiException {
        for (double radius : SEARCH_RADII) {
            List<Station> stations = fetchStations(boundsAround(origin, radius), origin);

            for (int index = 0; index < Math.min(MAX_STATION_ATTEMPTS, stations.size()); index++) {
                StationDetail detail;
                try {
                    detail = fetchDetail(stations.get(index).getIdx());
                } catch (ApiException unavailable) {
                    // Skip stations whose detail request fails and try the next one.
                    continue;
                }
                if (detail != null) {
                    return detail;
                }
            }
        }

        throw new ApiException(404, R.string.error_no_station);
    }

    /**
     * Fetches stations inside the given bounds, by name when no origin is given.
     */
    public List<Station> fetchStations(Bounds bounds, Coordinates origin) throws ApiException {
        String url = stationsUrl(bounds);
        String cacheKey = origin == null
                ? url
                : url + "|" + origin.getLat() + "," + origin.getLon();

        return stationLists.getOrLoad(cacheKey, STATION_TTL_MS, () -> {
            BoundsWire.Response json = Upstream.fetchJson(
                    url, BoundsWire.Response.class,
                    Upstream.Options.get(Upstream.BROWSER_HEADERS));
            return stationParser.parse(json, origin);
        });
    }

    public List<Station> fetchStations(Bounds bounds) throws ApiException {
        return fetchStations(bounds, null);
    }

    public List<Station> fetchStations() throws ApiException {
        return fetchStations(WORLD_BOUNDS, null);
    }

    /**
     * Fetches and caches the detail report for one station.
     *
     * @param idx WAQI station id; the provider is selected from its prefix.
     * @return The report, or {@code null} when the provider has no reading.
     */
    public StationDetail fetchDetail(String idx) throws ApiException {
        StationSource source = idx.startsWith("A") ? airnet : api2;

        return stationDetails.getOrLoad(idx, STATION_TTL_MS, () -> source.fetch(idx));
    }

    private Bounds boundsAround(Coordinates origin, double radius) {
        return new Bounds(
                origin.getLat() - radius,
                origin.getLon() - radius,
                origin.getLat() + radius,
                origin.getLon() + radius);
    }

    private String stationsUrl(Bounds bounds) {
        return HttpUrl.get(STATIONS_URL).newBuilder()
                .addQueryParameter("bounds", String.format(Locale.US, "%s,%s,%s,%s",
                        bounds.getWest(), bounds.getSouth(),
                        bounds.getEast(), bounds.getNorth()))
                .addQueryParameter("inc", "placeholders")
                .addQueryParameter("viewer", "webgl")
                .addQueryParameter("markers", "all")
                .build()
                .toString();
    }
}
