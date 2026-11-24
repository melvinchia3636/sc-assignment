package dev.melvinchia3636.airngreen.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.melvinchia3636.airngreen.api.AqiApiClient;
import dev.melvinchia3636.airngreen.api.ApiException;
import dev.melvinchia3636.airngreen.api.HttpTransport;
import dev.melvinchia3636.airngreen.model.District;
import dev.melvinchia3636.airngreen.model.StationReading;
import dev.melvinchia3636.airngreen.model.StationSummary;

class AirQualityServiceTest {

    private static final String STATION_JSON = """
            {"station":{"idx":"2609","name":"Kapit","aqi":25,"updatedAt":10,
             "current":{"pm25":13},"series":{"aqi":[{"t":1,"v":25}]}}}
            """;
    private static final String STATIONS_JSON = """
            {"stations":[{"idx":"2609","name":"Kapit","latitude":2.0,
             "longitude":112.9,"aqi":25}]}
            """;

    @TempDir
    Path directory;

    private final AtomicBoolean failing = new AtomicBoolean();
    private AirQualityService service;

    @BeforeEach
    void createService() {
        LocalStore store = new LocalStore(directory);
        AqiApiClient client = new AqiApiClient(url -> failing.get()
                ? new HttpTransport.Response(503, "")
                : new HttpTransport.Response(200,
                        url.contains("/stations") ? STATIONS_JSON : STATION_JSON),
                "https://example.test");
        service = new AirQualityService(client, store);
    }

    @Test
    void nearest_storesTheReading() throws ApiException {
        StationReading reading = service.nearest(District.KUALA_LUMPUR);

        assertAll(
                () -> assertEquals("2609", reading.idx()),
                () -> assertTrue(service.cachedReading().isPresent()),
                () -> assertEquals(25,
                        service.cachedReading().orElseThrow().aqi()));
    }

    @Test
    void nearest_whenApiFails_keepsTheCachedReading() throws ApiException {
        service.nearest(District.KUALA_LUMPUR);
        failing.set(true);

        assertThrows(ApiException.class,
                () -> service.nearest(District.KUALA_LUMPUR));
        assertTrue(service.cachedReading().isPresent());
    }

    @Test
    void byIdx_storesTheReading() throws ApiException {
        StationReading reading = service.byIdx("2609");

        assertAll(
                () -> assertEquals("2609", reading.idx()),
                () -> assertTrue(service.cachedReading().isPresent()));
    }

    @Test
    void stations_areCached() throws ApiException {
        List<StationSummary> stations = service.stations();

        assertAll(
                () -> assertEquals(1, stations.size()),
                () -> assertEquals(1, service.cachedStations().size()));
    }

    @Test
    void cachedReading_isEmptyBeforeAnyLoad() {
        assertTrue(service.cachedReading().isEmpty());
    }
}
