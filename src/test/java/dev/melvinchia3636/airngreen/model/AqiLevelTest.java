package dev.melvinchia3636.airngreen.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AqiLevelTest {

    @ParameterizedTest(name = "AQI {0} is {1}")
    @CsvSource({"0, GOOD", "50, GOOD", "51, MODERATE", "100, MODERATE",
        "101, SENSITIVE", "150, SENSITIVE", "151, UNHEALTHY", "200, UNHEALTHY",
        "201, VERY_UNHEALTHY", "300, VERY_UNHEALTHY", "301, HAZARDOUS",
        "500, HAZARDOUS"})
    void classify_mapsBandBoundaries(double aqi, AqiLevel expected) {
        assertEquals(expected, AqiLevel.classify(aqi));
    }

    @Test
    void classify_rejectsInvalidReadings() {
        assertAll(
            () -> assertEquals(AqiLevel.UNKNOWN, AqiLevel.classify(-1)),
            () -> assertEquals(AqiLevel.UNKNOWN, AqiLevel.classify(Double.NaN)),
            () -> assertEquals(
                AqiLevel.UNKNOWN,
                AqiLevel.classify(Double.POSITIVE_INFINITY)
            )
        );
    }

    @Test
    void isWorseThan_ordersBands() {
        assertAll(
            () -> assertTrue(AqiLevel.UNHEALTHY.isWorseThan(AqiLevel.MODERATE)),
            () -> assertFalse(
                AqiLevel.MODERATE.isWorseThan(AqiLevel.UNHEALTHY)
            ),
            () -> assertFalse(AqiLevel.GOOD.isWorseThan(AqiLevel.GOOD))
        );
    }

    @Test
    void isAtLeast_includesTheSameBand() {
        assertAll(
            () -> assertTrue(AqiLevel.SENSITIVE.isAtLeast(AqiLevel.SENSITIVE)),
            () -> assertTrue(AqiLevel.HAZARDOUS.isAtLeast(AqiLevel.GOOD)),
            () -> assertFalse(AqiLevel.GOOD.isAtLeast(AqiLevel.MODERATE))
        );
    }
}
