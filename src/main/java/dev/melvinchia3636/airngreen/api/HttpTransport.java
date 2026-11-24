package dev.melvinchia3636.airngreen.api;

import java.io.IOException;

/**
 * Minimal HTTP abstraction used by {@link AqiApiClient}.
 *
 * <p>Tests provide a stub implementation so no network is needed.</p>
 */
public interface HttpTransport {

    /**
     * Performs a GET request and returns the raw response.
     *
     * @param url absolute URL to request
     * @return status code and body
     * @throws IOException when the request cannot be completed
     */
    Response get(String url) throws IOException;

    /**
     * Raw HTTP response.
     *
     * @param statusCode HTTP status code
     * @param body       response body, possibly empty
     */
    record Response(int statusCode, String body) {
    }
}
