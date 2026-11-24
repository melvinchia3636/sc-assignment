package dev.melvinchia3636.airngreen.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import dev.melvinchia3636.airngreen.model.StationReading;
import dev.melvinchia3636.airngreen.model.StationSummary;

class AqiApiClientTest {

    private static final String STATION_JSON = """
            {"station":{"idx":"2609","name":"Kapit","aqi":25,"updatedAt":10,
             "current":{"pm25":13},"series":{"aqi":[{"t":1,"v":25}]}}}
            """;

    @Test
    void nearestStation_parsesResponseAndBuildsQuery() throws ApiException {
        AtomicReference<String> requested = new AtomicReference<>();
        AqiApiClient client = client(url -> {
            requested.set(url);
            return new HttpTransport.Response(200, STATION_JSON);
        });

        StationReading reading = client.nearestStation(3.139, 101.6869);

        assertAll(
                () -> assertEquals("2609", reading.idx()),
                () -> assertTrue(requested.get().startsWith(
                        "https://example.test/nearest-station?")),
                () -> assertTrue(requested.get().contains("lat=3.139")),
                () -> assertTrue(requested.get().contains("lon=101.6869")));
    }

    @Test
    void station_requestsTheEncodedId() throws ApiException {
        AtomicReference<String> requested = new AtomicReference<>();
        AqiApiClient client = client(url -> {
            requested.set(url);
            return new HttpTransport.Response(200, STATION_JSON);
        });

        client.station("A2820325");

        assertEquals("https://example.test/station/A2820325", requested.get());
    }

    @Test
    void stations_readsTheDirectory() throws ApiException {
        String body = """
                {"stations":[{"idx":"2609","name":"Kapit","latitude":2.0,
                 "longitude":112.9,"aqi":25}]}
                """;
        AqiApiClient client = client(
                url -> new HttpTransport.Response(200, body));

        List<StationSummary> stations = client.stations();

        assertAll(
                () -> assertEquals(1, stations.size()),
                () -> assertEquals("Kapit", stations.get(0).name()));
    }

    @Test
    void boundedStations_sendTheBoundingBox() throws ApiException {
        AtomicReference<String> requested = new AtomicReference<>();
        AqiApiClient client = client(url -> {
            requested.set(url);
            return new HttpTransport.Response(200, "{\"stations\":[]}");
        });

        client.stations(0.85, 99.6, 7.5, 119.3);

        assertAll(
                () -> assertTrue(requested.get().contains("south=0.8500")),
                () -> assertTrue(requested.get().contains("west=99.6000")),
                () -> assertTrue(requested.get().contains("north=7.5000")),
                () -> assertTrue(requested.get().contains("east=119.3000")));
    }

    @Test
    void notFound_usesTheApiMessage() {
        String body = "{\"error\":{\"code\":\"station_not_found\","
                + "\"message\":\"Station 9 did not return a reading.\"}}";
        AqiApiClient client = client(
                url -> new HttpTransport.Response(404, body));

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
        AqiApiClient client = client(
                url -> new HttpTransport.Response(503, ""));

        ApiException failure = assertThrows(ApiException.class,
                () -> client.nearestStation(1, 2));

        assertAll(
                () -> assertEquals(503, failure.getStatusCode()),
                () -> assertTrue(failure.isServiceFailure()),
                () -> assertTrue(failure.getMessage().contains("unavailable")));
    }

    @Test
    void unreadableBody_throwsApiException() {
        AqiApiClient client = client(
                url -> new HttpTransport.Response(200, "<html>nope</html>"));

        assertThrows(ApiException.class, () -> client.nearestStation(1, 2));
    }

    @Test
    void responseWithoutStation_throwsApiException() {
        AqiApiClient client = client(
                url -> new HttpTransport.Response(200, "{}"));

        assertThrows(ApiException.class, () -> client.nearestStation(1, 2));
    }

    @Test
    void ioFailure_becomesNetworkFailure() {
        AqiApiClient client = client(url -> {
            throw new IOException("offline");
        });

        ApiException failure = assertThrows(ApiException.class,
                () -> client.station("2609"));

        assertAll(
                () -> assertEquals(0, failure.getStatusCode()),
                () -> assertTrue(failure.isServiceFailure()),
                () -> assertTrue(failure.getMessage().contains("Cannot reach")));
    }

    @Test
    void isAlive_trueOnlyForNoContent() {
        AqiApiClient alive = client(
                url -> new HttpTransport.Response(204, ""));
        AqiApiClient down = client(
                url -> new HttpTransport.Response(500, ""));

        assertAll(
                () -> assertTrue(alive.isAlive()),
                () -> assertFalse(down.isAlive()));
    }

    @Test
    void baseUrl_trailingSlashIsNormalised() throws ApiException {
        AtomicReference<String> requested = new AtomicReference<>();
        AqiApiClient client = new AqiApiClient(url -> {
            requested.set(url);
            return new HttpTransport.Response(200, STATION_JSON);
        }, "https://example.test/");

        client.station("2609");

        assertEquals("https://example.test/station/2609", requested.get());
    }

    private AqiApiClient client(HttpTransport transport) {
        return new AqiApiClient(transport, "https://example.test");
    }
}
