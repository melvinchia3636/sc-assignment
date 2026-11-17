package com.groupone.assignment.guidance;

import androidx.annotation.StringRes;

import com.mikepenz.iconics.typeface.IIcon;

/**
 * Strategy for one AQI band: its guidance, colour and face icon.
 */
public abstract class AqiLevelStrategy {

    @StringRes
    private final int labelRes;
    @StringRes
    private final int adviceRes;
    private final String colourHex;
    private final IIcon icon;
    private final double maxAqi;

    protected AqiLevelStrategy(@StringRes int labelRes, @StringRes int adviceRes,
                               String colourHex, IIcon icon, double maxAqi) {
        this.labelRes = labelRes;
        this.adviceRes = adviceRes;
        this.colourHex = colourHex;
        this.icon = icon;
        this.maxAqi = maxAqi;
    }

    @StringRes
    public final int labelRes() {
        return labelRes;
    }

    @StringRes
    public final int adviceRes() {
        return adviceRes;
    }

    public final String colourHex() {
        return colourHex;
    }

    public final IIcon icon() {
        return icon;
    }

    /** Inclusive upper bound of this band. */
    public final double maxAqi() {
        return maxAqi;
    }

    public final boolean matches(double aqi) {
        return !Double.isNaN(aqi) && aqi <= maxAqi;
    }
}
