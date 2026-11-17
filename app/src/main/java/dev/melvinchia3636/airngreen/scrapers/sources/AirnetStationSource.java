package dev.melvinchia3636.airngreen.scrapers.sources;

import dev.melvinchia3636.airngreen.R;
import dev.melvinchia3636.airngreen.repositories.ApiException;
import dev.melvinchia3636.airngreen.repositories.model.StationDetail;
import dev.melvinchia3636.airngreen.scrapers.lib.TtlCache;
import dev.melvinchia3636.airngreen.scrapers.lib.Upstream;
import dev.melvinchia3636.airngreen.scrapers.parsers.AirnetParser;
import dev.melvinchia3636.airngreen.repositories.remote.AirnetRepository;
import dev.melvinchia3636.airngreen.repositories.remote.CwopRepository;

import okhttp3.HttpUrl;

/**
 * Fetches AirNet station reports (WAQI ids beginning with {@code A}).
 *
 * AirNet uses one shared API token rather than a token per station. A report
 * combines the hourly particulate feed with optional CWOP weather data.
 */
public class AirnetStationSource implements StationSource {

    private static final HttpUrl AIRNET_BASE = HttpUrl.get("https://airnet.waqi.info");
    private static final long TOKEN_TTL_MS = 30 * 60 * 1000;
    private static final String TOKEN_CACHE_KEY = "airnet";

    private final TtlCache<String> tokens = new TtlCache<>();
    private final AirnetParser parser = new AirnetParser();

    @Override
    public StationDetail fetch(String idx) throws ApiException {
        String token = tokens.getOrLoad(TOKEN_CACHE_KEY, TOKEN_TTL_MS, this::requestToken);

        // AirNet station ids are prefixed with "A"; the feed takes the numeric id.
        String id = idx.substring(1);

        AirnetRepository.Response hourly = Upstream.fetchJson(
                feedUrl("hourly", id, token).toString(),
                AirnetRepository.Response.class,
                Upstream.Options.get(Upstream.BROWSER_HEADERS));

        CwopRepository.Response cwop = null;
        try {
            cwop = Upstream.fetchJson(
                    feedUrl("cwop", id, token).toString(),
                    CwopRepository.Response.class,
                    Upstream.Options.get(Upstream.BROWSER_HEADERS));
        } catch (ApiException weatherUnavailable) {
            // Weather is optional; an unavailable CWOP feed should not fail the station.
        }

        return parser.parse(hourly, cwop, idx);
    }

    private String requestToken() throws ApiException {
        AirnetRepository.TokenResponse response = Upstream.fetchJson(
                AIRNET_BASE.newBuilder()
                        .addPathSegments("airnet/token")
                        .build()
                        .toString(),
                AirnetRepository.TokenResponse.class,
                Upstream.Options.post(Upstream.FORM_HEADERS, ""));

        if (response.key == null || response.key.isEmpty()) {
            throw new ApiException(ApiException.INVALID_RESPONSE, R.string.error_airnet_key);
        }
        return response.key;
    }

    private HttpUrl feedUrl(String feed, String id, String token) {
        return AIRNET_BASE.newBuilder()
                .addPathSegments("airnet/feed")
                .addPathSegment(feed)
                .addPathSegment(id)
                .addQueryParameter("key", token)
                .build();
    }
}
