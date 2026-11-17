package com.groupone.assignment.ui.aqi;

import android.annotation.SuppressLint;

import com.google.android.material.color.utilities.Contrast;
import com.google.android.material.color.utilities.Hct;

/**
 * Presentation helpers for AQI values.
 */
public final class AqiScale {

    private static final String DASH = "--";

    private AqiScale() {
    }

    /**
     * Formats a measurement for display, using a dash for missing values.
     */
    public static String format(double value) {
        return Double.isNaN(value) ? DASH : String.valueOf(Math.round(value));
    }

    /**
     * White unless the background cannot carry it. Uses Material Color
     * Utilities' HCT tone + WCAG contrast; {@code Hct.fromInt} is public but
     * marked {@code @RestrictedApi}, hence the deliberate suppression. 3.0 is
     * the WCAG large-text threshold for the card's 56sp/18sp text.
     */
    @SuppressLint("RestrictedApi")
    public static String textColourFor(String hex) {
        String value = hex.startsWith("#") ? hex.substring(1) : hex;
        int argb = 0xFF000000 | Integer.parseInt(value, 16);
        double tone = Hct.fromInt(argb).getTone();
        double whiteContrast = Contrast.ratioOfTones(100.0, tone);
        return whiteContrast >= 3.0 ? "#FFFFFF" : "#111827";
    }
}
