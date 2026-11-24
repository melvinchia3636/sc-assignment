package dev.melvinchia3636.airngreen.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.melvinchia3636.airngreen.model.StationReading;

class AqiApiClientTest {

    private static final String STATION_JSON = """
            {"station":{"idx":"2609","name":"Kapit","aqi":25,"updatedAt":10,
             "current":{"pm25":13},"series":{"aqi":[{"t":1,"v":25}]}}}
            """;

    private MockWebServer server;
    private AqiApiClient client;

    @BeforeEach
    void startServer() throws IOException {
        server = new MockWebServer();
        server.start();
        client = new AqiApiClient(server.url("/").toString());
    }

    @AfterEach
    void stopServer() throws IOException {
        server.shutdown();
    }

    @Test
    void nearestStation_parsesResponseAndBuildsQuery() throws Exception {
        server.enqueue(new MockResponse().setBody(STATION_JSON));

        StationReading reading = client.nearestStation(3.139, 101.6869);
        RecordedRequest request = server.takeRequest();

        assertAll(
                () -> assertEquals("2609", reading.idx()),
                () -> assertTrue(request.getPath().startsWith(
                        "/nearest-station?")),
                () -> assertTrue(request.getPath().contains("lat=3.139")),
                () -> assertTrue(request.getPath().contains("lon=101.6869")));
    }

    @Test
    void station_requestsTheEncodedId() throws Exception {
        server.enqueue(new MockResponse().setBody(STATION_JSON));

        client.station("A2820325");

        assertEquals("/station/A2820325", server.takeRequest().getPath());
    }

    @Test
    void defaultConstructor_usesTheConfiguredBaseUrl() throws Exception {
        String previous = System.getProperty("airngreen.apiBase");
        try {
            System.setProperty("airngreen.apiBase",
                    server.url("/").toString());
            server.enqueue(new MockResponse().setBody(STATION_JSON));

            new AqiApiClient().station("2609");

            assertEquals("/station/2609", server.takeRequest().getPath());
        } finally {
            if (previous == null) {
                System.clearProperty("airngreen.apiBase");
            } else {
                System.setProperty("airngreen.apiBase", previous);
            }
        }
    }

    @Test
    void boundedStations_sendTheBoundingBox() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"stations\":[]}"));

        client.stations(0.85, 99.6, 7.5, 119.3);

        String path = server.takeRequest().getPath();
        assertAll(
                () -> assertTrue(path.contains("south=0.85")),
                () -> assertTrue(path.contains("west=99.6")),
                () -> assertTrue(path.contains("north=7.5")),
                () -> assertTrue(path.contains("east=119.3")));
    }

    @Test
    void notFound_usesTheApiMessage() {
        String body = "{\"error\":{\"code\":\"station_not_found\","
                + "\"message\":\"Station 9 did not return a reading.\"}}";
        server.enqueue(new MockResponse().setResponseCode(404).setBody(body));

        ApiException failure = assertThrows(ApiException.class,
                () -> client.station("9"));

        assertAll(
                () -> assertEquals(404, failure.getStatusCode()),
                () -> assertEquals("Station 9 did not return a reading.",
                        failure.getMessage()),
                () -> assertFalse(failure.isServiceFailure()));
    }

    @Test
    void serverError_fallsBackToFriendlyMessage() {
        server.enqueue(new MockResponse().setResponseCode(503));

        ApiException failure = assertThrows(ApiException.class,
                () -> client.nearestStation(1, 2));

        assertAll(
                () -> assertEquals(503, failure.getStatusCode()),
                () -> assertTrue(failure.isServiceFailure()),
                () -> assertTrue(failure.getMessage().contains("unavailable")));
    }

    @Test
    void unreadableBody_throwsApiException() {
        server.enqueue(new MockResponse().setBody("<html>nope</html>"));

        assertThrows(ApiException.class, () -> client.nearestStation(1, 2));
    }

    @Test
    void responseWithoutStation_throwsApiException() {
        server.enqueue(new MockResponse().setBody("{}"));

        assertThrows(ApiException.class, () -> client.nearestStation(1, 2));
    }

    @Test
    void ioFailure_becomesNetworkFailure() throws IOException {
        MockWebServer dead = new MockWebServer();
        dead.start();
        String url = dead.url("/").toString();
        dead.shutdown();
        AqiApiClient offline = new AqiApiClient(url);

        ApiException failure = assertThrows(ApiException.class,
                () -> offline.station("2609"));

        assertAll(
                () -> assertEquals(0, failure.getStatusCode()),
                () -> assertTrue(failure.isServiceFailure()),
                () -> assertTrue(failure.getMessage().contains("Cannot reach")));
    }

    @Test
    void baseUrl_trailingSlashIsNormalised() throws Exception {
        server.enqueue(new MockResponse().setBody(STATION_JSON));

        client.station("2609");

        assertEquals("/station/2609", server.takeRequest().getPath());
    }
}
