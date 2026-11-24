package dev.melvinchia3636.airngreen.service.advice;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import dev.melvinchia3636.airngreen.model.AqiLevel;

class AdviceStrategiesTest {

    static Stream<AdviceStrategy> strategies() {
        return AdviceStrategies.getAll().stream();
    }

    @ParameterizedTest
    @MethodSource("strategies")
    void advice_isNotEmptyForEveryBand(AdviceStrategy strategy) {
        for (AqiLevel level : AqiLevel.values()) {
            assertFalse(strategy.advice(level).isBlank(),
                    () -> strategy.name() + " has no advice for " + level);
        }
    }

    @ParameterizedTest
    @CsvSource({ "general", "children", "elderly" })
    void byKey_returnsTheMatchingStrategy(String key) {
        assertEquals(key, AdviceStrategies.byKey(key).key());
    }

    @Test
    void byKey_fallsBackToGeneralAdvice() {
        assertSame(GeneralAdvice.class,
                AdviceStrategies.byKey("unknown-audience").getClass());
    }

    @Test
    void outdoorMinutes_fallAsAirQualityWorsens() {
        AdviceStrategy general = new GeneralAdvice();
        assertAll(
                () -> assertEquals(120, general.outdoorMinutes(AqiLevel.GOOD)),
                () -> assertEquals(60, general.outdoorMinutes(AqiLevel.MODERATE)),
                () -> assertEquals(30, general.outdoorMinutes(AqiLevel.SENSITIVE)),
                () -> assertEquals(15, general.outdoorMinutes(AqiLevel.UNHEALTHY)),
                () -> assertEquals(0, general.outdoorMinutes(AqiLevel.VERY_UNHEALTHY)));
    }

    @Test
    void childrenAreAdvisedLessOutdoorTimeThanAdults() {
        AdviceStrategy general = new GeneralAdvice();
        AdviceStrategy children = new ChildrenAdvice();

        assertTrue(children.outdoorMinutes(AqiLevel.GOOD)
                < general.outdoorMinutes(AqiLevel.GOOD));
    }

    @Test
    void elderlyAdvice_avoidsOutdoorsWhenUnhealthy() {
        assertEquals(0, new ElderlyAdvice().outdoorMinutes(AqiLevel.UNHEALTHY));
    }
}
