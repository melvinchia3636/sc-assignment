package dev.melvinchia3636.airngreen.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.melvinchia3636.airngreen.api.AqiApiClient;
import dev.melvinchia3636.airngreen.api.ApiException;
import dev.melvinchia3636.airngreen.model.District;
import dev.melvinchia3636.airngreen.model.StationReading;
import dev.melvinchia3636.airngreen.model.StationSummary;
import dev.melvinchia3636.airngreen.storage.LocalStore;

class AirQualityServiceTest {

    private static final String STATION_JSON = """
            {"station":{"idx":"2609","name":"Kapit","aqi":25,"updatedAt":10,
             "current":{"pm25":13},"series":{"aqi":[{"t":1,"v":25}]}}}
            """;
    private static final String STATIONS_JSON = """
            {"stations":[{"idx":"2609","name":"Kapit","latitude":2.0,
             "longitude":112.9,"aqi":25}]}
            """;
    private static final String LOCAL_STATIONS_JSON = """
            {"stations":[{"idx":"local","name":"Nearby","latitude":3.14,
             "longitude":101.69,"aqi":10}]}
            """;

    @TempDir
    Path directory;

    private final AtomicBoolean failing = new AtomicBoolean();
    private MockWebServer server;
    private AirQualityService service;

    @BeforeEach
    void createService() throws IOException {
        server = new MockWebServer();
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (failing.get()) {
                    return new MockResponse().setResponseCode(503);
                }

                String path = request.getPath();
                String body = path.contains("south=") ? LOCAL_STATIONS_JSON
                        : path.startsWith("/stations") ? STATIONS_JSON : STATION_JSON;
                return new MockResponse().setResponseCode(200).setBody(body);
            }
        });
        server.start();

        LocalStore store = new LocalStore(directory);
        AqiApiClient client =
                new AqiApiClient(server.url("/").toString());
        service = new AirQualityService(client, store);
    }

    @AfterEach
    void stopServer() throws IOException {
        server.shutdown();
    }

    @Test
    void nearest_storesTheReading() throws ApiException {
        StationReading reading = service.nearest(District.KUALA_LUMPUR);

        assertAll(
                () -> assertEquals("2609", reading.idx()),
                () -> assertTrue(service.getCachedReading().isPresent()),
                () -> assertEquals(25,
                        service.getCachedReading().orElseThrow().aqi()));
    }

    @Test
    void nearest_whenApiFails_keepsTheCachedReading() throws ApiException {
        service.nearest(District.KUALA_LUMPUR);
        failing.set(true);

        assertThrows(ApiException.class,
                () -> service.nearest(District.KUALA_LUMPUR));
        assertTrue(service.getCachedReading().isPresent());
    }

    @Test
    void byIdx_storesTheReading() throws ApiException {
        StationReading reading = service.byIdx("2609");

        assertAll(
                () -> assertEquals("2609", reading.idx()),
                () -> assertTrue(service.getCachedReading().isPresent()));
    }

    @Test
    void stations_areCached() throws ApiException {
        List<StationSummary> stations =
                service.stations(District.KUALA_LUMPUR);

        assertAll(
                () -> assertEquals(2, stations.size()),
                () -> assertEquals("local", stations.get(0).idx()),
                () -> assertEquals(2, service.getCachedStations().size()));
    }

    @Test
    void cachedReading_isEmptyBeforeAnyLoad() {
        assertTrue(service.getCachedReading().isEmpty());
    }
}
