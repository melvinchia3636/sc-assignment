package dev.melvinchia3636.airngreen.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.melvinchia3636.airngreen.model.SeriesPoint;
import dev.melvinchia3636.airngreen.model.StationReading;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Tests against the deployed service.
 *
 * <p>
 * Tagged {@code live} and excluded from the default build. Run them with
 * {@code mvn test -Plive-tests}.
 */
@Tag("live")
class AqiApiLiveTest {

    private static final String UNKNOWN_STATION = "999999999";
    private static final String SAMPLE_STATION = "2609";
    private static final int ADVERTISED_WINDOW_HOURS = 72;
    private static final long CLOCK_SLACK_SECONDS = 3600L;

    private final AqiApiClient client = new AqiApiClient(
        AqiApiClient.DEFAULT_BASE_URL
    );

    @Test
    void unknownStation_reportsNotFound() {
        ApiException failure = assertThrows(
            ApiException.class,
            () -> client.station(UNKNOWN_STATION)
        );

        assertEquals(404, failure.getStatusCode());
    }

    /**
     * Verifies the series against the contract the application relies on.
     *
     * <p>
     * WAQI advertises a 72-hour series but omits intervals the station did not
     * report and may include {@code null} samples, which the parser drops. The
     * series is therefore sparse and its length is not fixed, so the test
     * asserts only what the UI may assume: at least one sample, no more than
     * one sample per hour, and every sample inside the advertised window.
     * Treating the sample count as evidence of completeness would make this
     * test flaky against a healthy upstream.
     *
     * <p>
     * The window ends at the station's last update, not at the current time: a
     * station that stopped reporting hours ago still returns its own 72-hour
     * window. Anchoring on {@code Instant.now()} would therefore fail even
     * though the upstream response is well formed.
     */
    @Test
    void sampleStation_seriesIsSparseButInsideTheAdvertisedWindow()
        throws ApiException {
        StationReading reading = client.station(SAMPLE_STATION);
        List<SeriesPoint> series = reading.series();

        assertFalse(
            series.isEmpty(),
            "the sample station should report at least one AQI sample"
        );

        long anchor = reading.updatedAt() > 0
            ? reading.updatedAt()
            : latestTimestamp(series);
        long earliest = anchor
            - (ADVERTISED_WINDOW_HOURS * 3600L + CLOCK_SLACK_SECONDS);
        long latest = anchor + CLOCK_SLACK_SECONDS;

        assertAll(
            () -> assertTrue(
                series.size() <= ADVERTISED_WINDOW_HOURS,
                "WAQI returns at most one sample per hour"
            ),
            () -> assertTrue(
                series.stream().allMatch(
                    point -> point.epochSeconds() >= earliest
                        && point.epochSeconds() <= latest
                ),
                "every sample should fall inside the advertised window"
            ),
            () -> assertTrue(
                series.stream()
                    .allMatch(point -> Double.isFinite(point.value())),
                "the parser should have skipped null or non-finite values"
            )
        );
    }

    private static long latestTimestamp(List<SeriesPoint> series) {
        return series.stream().mapToLong(SeriesPoint::epochSeconds).max()
            .orElse(0L);
    }
}
