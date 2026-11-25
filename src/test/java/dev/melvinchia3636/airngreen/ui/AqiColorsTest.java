package dev.melvinchia3636.airngreen.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Color;

import org.junit.jupiter.api.Test;

import dev.melvinchia3636.airngreen.model.AqiLevel;

class AqiColorsTest {

    @Test
    void everyLevelHasAColour() {
        for (AqiLevel level : AqiLevel.values()) {
            assertNotNull(AqiColors.forLevel(level), level::name);
        }
    }

    @Test
    void unknownOrMissingLevelsFallBackToGrey() {
        assertEquals(
            AqiColors.forLevel(AqiLevel.UNKNOWN),
            AqiColors.forLevel(null)
        );
    }

    @Test
    void messageColourFollowsTheWarningFlag() {
        assertEquals(
            AqiColors.forLevel(AqiLevel.SENSITIVE),
            AqiColors.forMessage(true)
        );
        assertEquals(
            AqiColors.forLevel(AqiLevel.GOOD),
            AqiColors.forMessage(false)
        );
    }

    @Test
    void coloursAreSharedInstances() {
        Color first = AqiColors.forLevel(AqiLevel.GOOD);
        Color second = AqiColors.forLevel(AqiLevel.GOOD);

        assertSame(first, second);
    }
}
