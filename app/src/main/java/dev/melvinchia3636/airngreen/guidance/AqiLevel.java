package dev.melvinchia3636.airngreen.guidance;

import dev.melvinchia3636.airngreen.R;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;

/**
 * US AQI bands and their classification.
 */
public enum AqiLevel implements AqiLevelStrategy {

    UNKNOWN(new Data(
            R.string.aqi_unknown,
            R.string.aqi_unknown_advice,
            "#9ca3af",
            FontAwesome.Icon.faw_meh,
            Double.NaN)),
    GOOD(new Data(
            R.string.aqi_good,
            R.string.aqi_good_advice,
            "#009966",
            FontAwesome.Icon.faw_smile,
            50)),
    MODERATE(new Data(
            R.string.aqi_moderate,
            R.string.aqi_moderate_advice,
            "#ffde33",
            FontAwesome.Icon.faw_meh,
            100)),
    SENSITIVE(new Data(
            R.string.aqi_sensitive,
            R.string.aqi_sensitive_advice,
            "#ff9933",
            FontAwesome.Icon.faw_frown_open,
            150)),
    UNHEALTHY(new Data(
            R.string.aqi_unhealthy,
            R.string.aqi_unhealthy_advice,
            "#cc0033",
            FontAwesome.Icon.faw_frown,
            200)),
    VERY_UNHEALTHY(new Data(
            R.string.aqi_very_unhealthy,
            R.string.aqi_very_unhealthy_advice,
            "#660099",
            FontAwesome.Icon.faw_tired,
            300)),
    HAZARDOUS(new Data(
            R.string.aqi_hazardous,
            R.string.aqi_hazardous_advice,
            "#7e0023",
            FontAwesome.Icon.faw_dizzy,
            Double.POSITIVE_INFINITY));

    private final Data data;

    AqiLevel(Data data) {
        this.data = data;
    }

    @Override
    public Data data() {
        return data;
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
}
