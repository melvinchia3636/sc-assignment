package com.groupone.assignment.guidance;

import com.mikepenz.iconics.typeface.IIcon;

/**
 * One AQI band and its guidance.
 */
public interface AqiLevelStrategy {

    int labelRes();

    int adviceRes();

    String colourHex();

    IIcon icon();

    /** Inclusive upper bound of this band. */
    double maxAqi();

    default boolean matches(double aqi) {
        return !Double.isNaN(aqi) && aqi <= maxAqi();
    }
}
