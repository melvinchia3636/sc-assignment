package com.groupone.assignment.ui.aqi;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.groupone.assignment.R;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;

import org.junit.jupiter.api.Test;

class AqiScaleTest {

    @Test
    void classify_boundaryValues_returnExpectedBand() {
        assertEquals(R.string.aqi_good, AqiScale.classify(0).getLabelRes());
        assertEquals(R.string.aqi_good, AqiScale.classify(50).getLabelRes());
        assertEquals(R.string.aqi_moderate, AqiScale.classify(51).getLabelRes());
        assertEquals(R.string.aqi_moderate, AqiScale.classify(100).getLabelRes());
        assertEquals(R.string.aqi_sensitive, AqiScale.classify(101).getLabelRes());
        assertEquals(R.string.aqi_sensitive, AqiScale.classify(150).getLabelRes());
        assertEquals(R.string.aqi_unhealthy, AqiScale.classify(151).getLabelRes());
        assertEquals(R.string.aqi_unhealthy, AqiScale.classify(200).getLabelRes());
        assertEquals(R.string.aqi_very_unhealthy, AqiScale.classify(201).getLabelRes());
        assertEquals(R.string.aqi_very_unhealthy, AqiScale.classify(300).getLabelRes());
        assertEquals(R.string.aqi_hazardous, AqiScale.classify(301).getLabelRes());
        assertEquals(R.string.aqi_hazardous, AqiScale.classify(500).getLabelRes());
    }

    @Test
    void classify_exposesInclusiveUpperBounds() {
        assertEquals(50, AqiScale.classify(50).getMaxAqi());
        assertEquals(100, AqiScale.classify(100).getMaxAqi());
        assertEquals(150, AqiScale.classify(150).getMaxAqi());
        assertEquals(200, AqiScale.classify(200).getMaxAqi());
        assertEquals(300, AqiScale.classify(300).getMaxAqi());
        assertTrue(Double.isInfinite(AqiScale.classify(301).getMaxAqi()));
        assertTrue(Double.isNaN(AqiScale.classify(Double.NaN).getMaxAqi()));
    }

    @Test
    void classify_missingValue_returnsUnknown() {
        assertEquals(R.string.aqi_unknown, AqiScale.classify(Double.NaN).getLabelRes());
    }

    @Test
    void classify_everyBand_hasResourcesColourAndIcon() {
        double[] samples = {10, 75, 125, 175, 250, 400};
        for (double sample : samples) {
            AqiScale.Level level = AqiScale.classify(sample);
            assertAll(
                    () -> assertNotEquals(0, level.getLabelRes()),
                    () -> assertNotEquals(0, level.getAdviceRes()),
                    () -> assertTrue(level.getColourHex().matches("#[0-9A-Fa-f]{6}")),
                    () -> assertNotNull(level.getIcon())
            );
        }
    }

    @Test
    void classify_facesMatchBands() {
        assertEquals(FontAwesome.Icon.faw_smile, AqiScale.classify(10).getIcon());
        assertEquals(FontAwesome.Icon.faw_meh, AqiScale.classify(75).getIcon());
        assertEquals(FontAwesome.Icon.faw_frown_open, AqiScale.classify(125).getIcon());
        assertEquals(FontAwesome.Icon.faw_frown, AqiScale.classify(175).getIcon());
        assertEquals(FontAwesome.Icon.faw_tired, AqiScale.classify(250).getIcon());
        assertEquals(FontAwesome.Icon.faw_dizzy, AqiScale.classify(400).getIcon());
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
