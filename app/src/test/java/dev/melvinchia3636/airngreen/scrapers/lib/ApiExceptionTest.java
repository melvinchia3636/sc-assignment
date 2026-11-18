package dev.melvinchia3636.airngreen.scrapers.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.melvinchia3636.airngreen.R;

import org.junit.jupiter.api.Test;

class ApiExceptionTest {

    @Test
    void http_404_returnsNoDataMessage() {
        ApiException failure = ApiException.http(404);

        assertEquals(404, failure.getStatusCode());
        assertEquals(R.string.error_no_data, failure.getMessageRes());
    }

    @Test
    void http_500_returnsUnavailableMessage() {
        ApiException failure = ApiException.http(500);

        assertEquals(500, failure.getStatusCode());
        assertEquals(R.string.error_unavailable, failure.getMessageRes());
    }

    @Test
    void networkFailure_isMarkedAsOffline() {
        ApiException failure = ApiException.network(new java.io.IOException("boom"));

        assertTrue(failure.isNetworkError());
        assertEquals(R.string.error_offline, failure.getMessageRes());
    }
}
