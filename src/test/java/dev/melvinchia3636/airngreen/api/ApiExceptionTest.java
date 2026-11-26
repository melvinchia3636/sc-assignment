package dev.melvinchia3636.airngreen.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ApiExceptionTest {

    @Test
    void messageFor_mapsEachStatusToAUserFacingMessage() {
        assertEquals(
            "Station not found. Check the station id and try again.",
            ApiException.messageFor(404)
        );
        assertEquals(
            "Too many requests. Please wait a moment and try again.",
            ApiException.messageFor(429)
        );
        assertEquals(
            "The air-quality service is unavailable. "
                + "Showing the last saved reading.",
            ApiException.messageFor(500)
        );
        assertEquals(
            "Unexpected response from the air-quality service (HTTP 418).",
            ApiException.messageFor(418)
        );
    }
}
