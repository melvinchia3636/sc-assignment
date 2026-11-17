package dev.melvinchia3636.airngreen.ui.common;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.melvinchia3636.airngreen.R;
import dev.melvinchia3636.airngreen.guidance.AqiLevel;
import dev.melvinchia3636.airngreen.guidance.AqiLevelStrategy;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;

import org.junit.jupiter.api.Test;

class AqiScaleTest {

    @Test
    void classify_boundaryValues_returnExpectedBand() {
        assertEquals(R.string.aqi_good, AqiLevel.classify(0).labelRes());
        assertEquals(R.string.aqi_good, AqiLevel.classify(50).labelRes());
        assertEquals(R.string.aqi_moderate, AqiLevel.classify(51).labelRes());
        assertEquals(R.string.aqi_moderate, AqiLevel.classify(100).labelRes());
        assertEquals(R.string.aqi_sensitive, AqiLevel.classify(101).labelRes());
        assertEquals(R.string.aqi_sensitive, AqiLevel.classify(150).labelRes());
        assertEquals(R.string.aqi_unhealthy, AqiLevel.classify(151).labelRes());
        assertEquals(R.string.aqi_unhealthy, AqiLevel.classify(200).labelRes());
        assertEquals(R.string.aqi_very_unhealthy, AqiLevel.classify(201).labelRes());
        assertEquals(R.string.aqi_very_unhealthy, AqiLevel.classify(300).labelRes());
        assertEquals(R.string.aqi_hazardous, AqiLevel.classify(301).labelRes());
        assertEquals(R.string.aqi_hazardous, AqiLevel.classify(500).labelRes());
    }

    @Test
    void classify_exposesInclusiveUpperBounds() {
        assertEquals(50, AqiLevel.classify(50).maxAqi());
        assertEquals(100, AqiLevel.classify(100).maxAqi());
        assertEquals(150, AqiLevel.classify(150).maxAqi());
        assertEquals(200, AqiLevel.classify(200).maxAqi());
        assertEquals(300, AqiLevel.classify(300).maxAqi());
        assertTrue(Double.isInfinite(AqiLevel.classify(301).maxAqi()));
        assertTrue(Double.isNaN(AqiLevel.classify(Double.NaN).maxAqi()));
    }

    @Test
    void classify_missingValue_returnsUnknown() {
        assertEquals(R.string.aqi_unknown, AqiLevel.classify(Double.NaN).labelRes());
    }

    @Test
    void classify_everyBand_hasResourcesColourAndIcon() {
        double[] samples = {10, 75, 125, 175, 250, 400};
        for (double sample : samples) {
            AqiLevelStrategy level = AqiLevel.classify(sample);
            assertAll(
                    () -> assertNotEquals(0, level.labelRes()),
                    () -> assertNotEquals(0, level.adviceRes()),
                    () -> assertTrue(level.colourHex().matches("#[0-9A-Fa-f]{6}")),
                    () -> assertNotNull(level.icon())
            );
        }
    }

    @Test
    void classify_facesMatchBands() {
        assertEquals(FontAwesome.Icon.faw_smile, AqiLevel.classify(10).icon());
        assertEquals(FontAwesome.Icon.faw_meh, AqiLevel.classify(75).icon());
        assertEquals(FontAwesome.Icon.faw_frown_open, AqiLevel.classify(125).icon());
        assertEquals(FontAwesome.Icon.faw_frown, AqiLevel.classify(175).icon());
        assertEquals(FontAwesome.Icon.faw_tired, AqiLevel.classify(250).icon());
        assertEquals(FontAwesome.Icon.faw_dizzy, AqiLevel.classify(400).icon());
    }

    @Test
    void textColour_usesDarkTextOnLightBands() {
        assertEquals("#111827", AqiScale.textColourFor("#ffde33"));
        assertEquals("#111827", AqiScale.textColourFor("#ff9933"));
        assertEquals("#FFFFFF", AqiScale.textColourFor("#009966"));
        assertEquals("#FFFFFF", AqiScale.textColourFor("#7e0023"));
    }

    @Test
    void format_roundsToWholeNumber() {
        assertEquals("12", AqiScale.format(12.4));
        assertEquals("13", AqiScale.format(12.6));
    }

    @Test
    void format_missingValue_returnsDash() {
        assertEquals("--", AqiScale.format(Double.NaN));
    }
}
