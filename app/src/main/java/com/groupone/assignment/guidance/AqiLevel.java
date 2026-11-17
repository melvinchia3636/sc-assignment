package com.groupone.assignment.guidance;

import androidx.annotation.StringRes;

import com.groupone.assignment.R;
import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;

/**
 * US AQI bands with their guidance, colours and face icon.
 */
public record AqiLevel(@StringRes int labelRes, @StringRes int adviceRes,
                       String colourHex, IIcon icon, double maxAqi)
        implements AqiLevelStrategy {

    public static final AqiLevel UNKNOWN = new AqiLevel(
            R.string.aqi_unknown, R.string.aqi_unknown_advice,
            "#9ca3af", FontAwesome.Icon.faw_meh, Double.NaN);
    public static final AqiLevel GOOD = new AqiLevel(
            R.string.aqi_good, R.string.aqi_good_advice,
            "#009966", FontAwesome.Icon.faw_smile, 50);
    public static final AqiLevel MODERATE = new AqiLevel(
            R.string.aqi_moderate, R.string.aqi_moderate_advice,
            "#ffde33", FontAwesome.Icon.faw_meh, 100);
    public static final AqiLevel SENSITIVE = new AqiLevel(
            R.string.aqi_sensitive, R.string.aqi_sensitive_advice,
            "#ff9933", FontAwesome.Icon.faw_frown_open, 150);
    public static final AqiLevel UNHEALTHY = new AqiLevel(
            R.string.aqi_unhealthy, R.string.aqi_unhealthy_advice,
            "#cc0033", FontAwesome.Icon.faw_frown, 200);
    public static final AqiLevel VERY_UNHEALTHY = new AqiLevel(
            R.string.aqi_very_unhealthy, R.string.aqi_very_unhealthy_advice,
            "#660099", FontAwesome.Icon.faw_tired, 300);
    public static final AqiLevel HAZARDOUS = new AqiLevel(
            R.string.aqi_hazardous, R.string.aqi_hazardous_advice,
            "#7e0023", FontAwesome.Icon.faw_dizzy, Double.POSITIVE_INFINITY);

    private static final AqiLevel[] ALL = {
            UNKNOWN, GOOD, MODERATE, SENSITIVE, UNHEALTHY, VERY_UNHEALTHY, HAZARDOUS,
    };

    public static AqiLevel classify(double aqi) {
        if (Double.isNaN(aqi)) {
            return UNKNOWN;
        }
        for (AqiLevel level : ALL) {
            if (level != UNKNOWN && level.matches(aqi)) {
                return level;
            }
        }
        return HAZARDOUS;
    }
}
