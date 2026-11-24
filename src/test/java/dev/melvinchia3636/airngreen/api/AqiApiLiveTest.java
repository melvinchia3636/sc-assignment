package dev.melvinchia3636.airngreen.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import dev.melvinchia3636.airngreen.model.StationReading;

/**
 * Tests against the deployed service.
 *
 * <p>Tagged {@code live} and excluded from the default build. Run them with
 * {@code mvn test -Plive-tests}.</p>
 */
@Tag("live")
class AqiApiLiveTest {

    private static final String UNKNOWN_STATION = "999999999";
    private static final String SAMPLE_STATION = "2609";
    private static final int EXPECTED_SERIES_SAMPLES = 72;

    private final AqiApiClient client =
            new AqiApiClient(AqiApiClient.DEFAULT_BASE_URL);

    @Test
    void liveness_answersWithNoContent() {
        assertTrue(client.isAlive());
    }

    @Test
    void unknownStation_reportsNotFound() {
        ApiException failure = assertThrows(ApiException.class,
                () -> client.station(UNKNOWN_STATION));

        assertEquals(404, failure.getStatusCode());
    }

    /**
     * Deliberately failing test.
     *
     * <p>The API advertises a 72-hour series, but WAQI omits intervals that the
     * station did not report. The application therefore draws a trend with
     * fewer than 72 samples and cannot promise a fixed resolution. This test
     * records that limitation instead of hiding it.</p>
     */
    @Test
    void sampleStation_hasAFullSeventyTwoSampleSeries() throws ApiException {
        StationReading reading = client.station(SAMPLE_STATION);

        assertEquals(EXPECTED_SERIES_SAMPLES, reading.series().size(),
                "WAQI omits intervals, so the series is shorter than 72 samples");
    }
}
