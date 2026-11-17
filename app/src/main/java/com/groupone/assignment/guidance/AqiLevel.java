package com.groupone.assignment.guidance;

import androidx.annotation.StringRes;

import com.groupone.assignment.R;
import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;

/**
 * US AQI bands with their guidance, colours and face icon.
 */
public enum AqiLevel implements AqiLevelStrategy {

    UNKNOWN(R.string.aqi_unknown, R.string.aqi_unknown_advice,
            "#9ca3af", FontAwesome.Icon.faw_meh, Double.NaN),
    GOOD(R.string.aqi_good, R.string.aqi_good_advice,
            "#009966", FontAwesome.Icon.faw_smile, 50),
    MODERATE(R.string.aqi_moderate, R.string.aqi_moderate_advice,
            "#ffde33", FontAwesome.Icon.faw_meh, 100),
    SENSITIVE(R.string.aqi_sensitive, R.string.aqi_sensitive_advice,
            "#ff9933", FontAwesome.Icon.faw_frown_open, 150),
    UNHEALTHY(R.string.aqi_unhealthy, R.string.aqi_unhealthy_advice,
            "#cc0033", FontAwesome.Icon.faw_frown, 200),
    VERY_UNHEALTHY(R.string.aqi_very_unhealthy, R.string.aqi_very_unhealthy_advice,
            "#660099", FontAwesome.Icon.faw_tired, 300),
    HAZARDOUS(R.string.aqi_hazardous, R.string.aqi_hazardous_advice,
            "#7e0023", FontAwesome.Icon.faw_dizzy, Double.POSITIVE_INFINITY);

    @StringRes
    private final int labelRes;
    @StringRes
    private final int adviceRes;
    private final String colourHex;
    private final IIcon icon;
    private final double maxAqi;

    AqiLevel(@StringRes int labelRes, @StringRes int adviceRes, String colourHex,
             IIcon icon, double maxAqi) {
        this.labelRes = labelRes;
        this.adviceRes = adviceRes;
        this.colourHex = colourHex;
        this.icon = icon;
        this.maxAqi = maxAqi;
    }

    public static AqiLevel classify(double aqi) {
        if (Double.isNaN(aqi)) {
            return UNKNOWN;
        }
        for (AqiLevel level : values()) {
            if (level != UNKNOWN && level.matches(aqi)) {
                return level;
            }
        }
        return HAZARDOUS;
    }

    @Override
    public int labelRes() {
        return labelRes;
    }

    @Override
    public int adviceRes() {
        return adviceRes;
    }

    @Override
    public String colourHex() {
        return colourHex;
    }

    @Override
    public IIcon icon() {
        return icon;
    }

    @Override
    public double maxAqi() {
        return maxAqi;
    }

    @StringRes
    public int getLabelRes() {
        return labelRes;
    }

    @StringRes
    public int getAdviceRes() {
        return adviceRes;
    }

    public String getColourHex() {
        return colourHex;
    }

    public IIcon getIcon() {
        return icon;
    }

    /** Inclusive upper bound of this band. */
    public double getMaxAqi() {
        return maxAqi;
    }
}
