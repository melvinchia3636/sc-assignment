package dev.melvinchia3636.airngreen.repositories.remote;

import com.google.gson.Gson;
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
    private static final Gson GSON = new Gson();

    private GoogleTilesRepository() {
    }

    /** A tile session token and the epoch second it stops being valid. */
    public record Session(String token, long expiryEpochSeconds) {
    }

    public static Session createSession(String apiKey, Map<String, String> headers)
            throws ApiException {
        HttpUrl url = HttpUrl.parse(SESSION_URL);
        if (url == null) {
            throw new ApiException(ApiException.INVALID_RESPONSE,
                    R.string.map_google_session_failed);
        }
        String endpoint = url.newBuilder().addQueryParameter("key", apiKey).build().toString();
        String body = GSON.toJson(new SessionRequest());

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

    private static final class SessionRequest {
        final String mapType = "roadmap";
        final String language = "en-US";
        final String region = "US";
    }

    private static final class SessionResponse {
        String session;
        long expiry;
    }
}
