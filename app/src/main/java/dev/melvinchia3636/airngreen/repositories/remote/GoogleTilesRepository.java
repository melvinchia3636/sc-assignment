package dev.melvinchia3636.airngreen.repositories.remote;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.melvinchia3636.airngreen.R;
import dev.melvinchia3636.airngreen.repositories.ApiException;
import dev.melvinchia3636.airngreen.scrapers.lib.Upstream;

import okhttp3.HttpUrl;

import java.util.Map;

/**
 * Creates Google Map Tiles API sessions for a user-supplied API key.
 */
public final class GoogleTilesRepository {

    private static final String SESSION_URL = "https://tile.googleapis.com/v1/createSession";

    private GoogleTilesRepository() {
    }

    /** A tile session token and the epoch second it stops being valid. */
    public record Session(String token, long expiryEpochSeconds) {
    }

    public static Session createSession(String apiKey, Map<String, String> headers,
                                        String styleJson) throws ApiException {
        HttpUrl url = HttpUrl.parse(SESSION_URL);
        if (url == null) {
            throw new ApiException(ApiException.INVALID_RESPONSE,
                    R.string.map_google_session_failed);
        }
        String endpoint = url.newBuilder().addQueryParameter("key", apiKey).build().toString();
        String body = sessionRequest(styleJson).toString();

        SessionResponse response;
        try {
            response = Upstream.fetchJson(
                    endpoint, SessionResponse.class,
                    Upstream.Options.postJson(headers, body));
        } catch (ApiException failure) {
            if (failure.getStatusCode() == 403 || failure.getStatusCode() == 400) {
                throw new ApiException(failure.getStatusCode(),
                        R.string.map_google_key_rejected, googleReason(failure.getDetail()),
                        failure);
            }
            throw failure;
        }
        if (response == null || response.session == null || response.session.isEmpty()) {
            throw new ApiException(ApiException.INVALID_RESPONSE,
                    R.string.map_google_session_failed);
        }
        return new Session(response.session, response.expiry);
    }

    /** Extracts the human-readable reason from a Google error payload. */
    private static String googleReason(String detail) {
        if (detail == null) {
            return null;
        }
        try {
            JsonObject error = JsonParser.parseString(detail).getAsJsonObject()
                    .getAsJsonObject("error");
            return error != null && error.has("message")
                    ? error.get("message").getAsString()
                    : null;
        } catch (RuntimeException notGoogleError) {
            return null;
        }
    }

    /**
     * Builds the session payload. Roadmap tiles are the only styleable map type,
     * so the Night style is applied by session rather than per tile request.
     */
    private static JsonObject sessionRequest(String styleJson) {
        JsonObject request = new JsonObject();
        request.addProperty("mapType", "roadmap");
        request.addProperty("language", "en-US");
        request.addProperty("region", "US");
        if (styleJson != null && !styleJson.isEmpty()) {
            request.add("styles", JsonParser.parseString(styleJson));
        }
        return request;
    }

    private static final class SessionResponse {
        String session;
        long expiry;
    }
}
