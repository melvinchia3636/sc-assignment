package com.groupone.assignment.guidance;

import com.groupone.assignment.R;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;

/**
 * AQI band strategies and band classification.
 */
public final class AqiLevel {

    public static final AqiLevelStrategy UNKNOWN = new UnknownLevel();
    public static final AqiLevelStrategy GOOD = new GoodLevel();
    public static final AqiLevelStrategy MODERATE = new ModerateLevel();
    public static final AqiLevelStrategy SENSITIVE = new SensitiveLevel();
    public static final AqiLevelStrategy UNHEALTHY = new UnhealthyLevel();
    public static final AqiLevelStrategy VERY_UNHEALTHY = new VeryUnhealthyLevel();
    public static final AqiLevelStrategy HAZARDOUS = new HazardousLevel();

    private static final AqiLevelStrategy[] ALL = {
            UNKNOWN, GOOD, MODERATE, SENSITIVE, UNHEALTHY, VERY_UNHEALTHY, HAZARDOUS,
    };

    private AqiLevel() {
    }

    public static AqiLevelStrategy classify(double aqi) {
        if (Double.isNaN(aqi)) {
            return UNKNOWN;
        }
        for (AqiLevelStrategy level : ALL) {
            if (level != UNKNOWN && level.matches(aqi)) {
                return level;
            }
        }
        return HAZARDOUS;
    }

    private static final class UnknownLevel extends AqiLevelStrategy {
        UnknownLevel() {
            super(R.string.aqi_unknown, R.string.aqi_unknown_advice,
                    "#9ca3af", FontAwesome.Icon.faw_meh, Double.NaN);
        }
    }

    private static final class GoodLevel extends AqiLevelStrategy {
        GoodLevel() {
            super(R.string.aqi_good, R.string.aqi_good_advice,
                    "#009966", FontAwesome.Icon.faw_smile, 50);
        }
    }

    private static final class ModerateLevel extends AqiLevelStrategy {
        ModerateLevel() {
            super(R.string.aqi_moderate, R.string.aqi_moderate_advice,
                    "#ffde33", FontAwesome.Icon.faw_meh, 100);
        }
    }

    private static final class SensitiveLevel extends AqiLevelStrategy {
        SensitiveLevel() {
            super(R.string.aqi_sensitive, R.string.aqi_sensitive_advice,
                    "#ff9933", FontAwesome.Icon.faw_frown_open, 150);
        }
    }

    private static final class UnhealthyLevel extends AqiLevelStrategy {
        UnhealthyLevel() {
            super(R.string.aqi_unhealthy, R.string.aqi_unhealthy_advice,
                    "#cc0033", FontAwesome.Icon.faw_frown, 200);
        }
    }

    private static final class VeryUnhealthyLevel extends AqiLevelStrategy {
        VeryUnhealthyLevel() {
            super(R.string.aqi_very_unhealthy, R.string.aqi_very_unhealthy_advice,
                    "#660099", FontAwesome.Icon.faw_tired, 300);
        }
    }

    private static final class HazardousLevel extends AqiLevelStrategy {
        HazardousLevel() {
            super(R.string.aqi_hazardous, R.string.aqi_hazardous_advice,
                    "#7e0023", FontAwesome.Icon.faw_dizzy, Double.POSITIVE_INFINITY);
        }
    }
}
