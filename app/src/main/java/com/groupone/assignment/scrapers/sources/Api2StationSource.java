package com.groupone.assignment.scrapers.sources;

import com.groupone.assignment.R;
import com.groupone.assignment.repositories.ApiException;
import com.groupone.assignment.repositories.model.StationDetail;
import com.groupone.assignment.lib.TtlCache;
import com.groupone.assignment.lib.Upstream;
import com.groupone.assignment.scrapers.parsers.Api2Parser;
import com.groupone.assignment.repositories.remote.Api2Repository;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import okhttp3.HttpUrl;

/**
 * Numeric WAQI stations: api2 token + feed flow.
 */
public class Api2StationSource implements StationSource {

    private static final HttpUrl API2_BASE = HttpUrl.get("https://api2.waqi.info");
    private static final long TOKEN_TTL_MS = 30 * 60 * 1000;

    private final TtlCache<String> tokens = new TtlCache<>();
    private final Api2Parser parser = new Api2Parser();

    @Override
    public StationDetail fetch(String idx) throws ApiException {
        String token = tokens.getOrLoad(idx, TOKEN_TTL_MS, () -> requestToken(idx));

        String body = "token=" + encode(token) + "&id=" + encode(idx);
        Api2Repository.Response feed = Upstream.fetchJson(
                feedUrl(idx).toString(),
                Api2Repository.Response.class,
                Upstream.Options.post(Upstream.FORM_HEADERS, body));

        return parser.parse(feed, idx);
    }

    private String requestToken(String idx) throws ApiException {
        Api2Repository.TokenResponse response = Upstream.fetchJson(
                API2_BASE.newBuilder()
                        .addPathSegments("api/token")
                        .addPathSegment(idx)
                        .build()
                        .toString(),
                Api2Repository.TokenResponse.class,
                Upstream.Options.post(Upstream.FORM_HEADERS, ""));

        if (response.rxs != null && response.rxs.obs != null) {
            for (Api2Repository.Observation observation : response.rxs.obs) {
                if (observation.msg != null && observation.msg.token != null
                        && !observation.msg.token.isEmpty()) {
                    return observation.msg.token;
                }
            }
        }
        throw new ApiException(ApiException.INVALID_RESPONSE, R.string.error_station_token);
    }

    private HttpUrl feedUrl(String idx) {
        return API2_BASE.newBuilder()
                .addPathSegments("api/feed")
                .addPathSegment("@" + idx)
                .addPathSegment("aqi.json")
                .build();
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException impossible) {
            return value;
        }
    }
}
