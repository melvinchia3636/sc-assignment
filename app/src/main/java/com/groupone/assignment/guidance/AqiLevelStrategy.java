package com.groupone.assignment.guidance;

import androidx.annotation.StringRes;

import com.mikepenz.iconics.typeface.IIcon;

/**
 * One AQI band and its guidance.
 */
public interface AqiLevelStrategy {

    @StringRes
    int labelRes();

    @StringRes
    int adviceRes();

    String colourHex();

    IIcon icon();

    /** Inclusive upper bound of this band. */
    double maxAqi();

    default boolean matches(double aqi) {
        return !Double.isNaN(aqi) && aqi <= maxAqi();
    }
}
