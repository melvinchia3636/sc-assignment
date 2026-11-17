package com.groupone.assignment.ui.aqi;

import android.annotation.SuppressLint;

import com.groupone.assignment.R;

import androidx.annotation.StringRes;

import com.google.android.material.color.utilities.Contrast;
import com.google.android.material.color.utilities.Hct;
import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;

/**
 * Maps a US AQI value to a band, advice, colours and face icon. Bands and
 * colours match the LifeForge air-quality module (0-50 good ... 301+ hazardous).
 */
public final class AqiScale {

    private static final String DASH = "--";

    private static final Level UNKNOWN = new Level(
            R.string.aqi_unknown,
            R.string.aqi_unknown_advice,
            "#9ca3af",
            FontAwesome.Icon.faw_meh,
            Double.NaN);

    private static final Level[] LEVELS = {
            new Level(R.string.aqi_good, R.string.aqi_good_advice,
                    "#009966", FontAwesome.Icon.faw_smile, 50),
            new Level(R.string.aqi_moderate, R.string.aqi_moderate_advice,
                    "#ffde33", FontAwesome.Icon.faw_meh, 100),
            new Level(R.string.aqi_sensitive, R.string.aqi_sensitive_advice,
                    "#ff9933", FontAwesome.Icon.faw_frown_open, 150),
            new Level(R.string.aqi_unhealthy, R.string.aqi_unhealthy_advice,
                    "#cc0033", FontAwesome.Icon.faw_frown, 200),
            new Level(R.string.aqi_very_unhealthy, R.string.aqi_very_unhealthy_advice,
                    "#660099", FontAwesome.Icon.faw_tired, 300),
            new Level(R.string.aqi_hazardous, R.string.aqi_hazardous_advice,
                    "#7e0023", FontAwesome.Icon.faw_dizzy, Double.POSITIVE_INFINITY),
    };

    private AqiScale() {
    }

    public static Level classify(double aqi) {
        if (Double.isNaN(aqi)) {
            return UNKNOWN;
        }
        for (Level level : LEVELS) {
            if (aqi <= level.getMaxAqi()) {
                return level;
            }
        }
        return LEVELS[LEVELS.length - 1];
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

    public static final class Level {

        @StringRes
        private final int labelRes;
        @StringRes
        private final int adviceRes;
        private final String colourHex;
        private final IIcon icon;
        private final double maxAqi;

        public Level(@StringRes int labelRes, @StringRes int adviceRes, String colourHex,
                     IIcon icon, double maxAqi) {
            this.labelRes = labelRes;
            this.adviceRes = adviceRes;
            this.colourHex = colourHex;
            this.icon = icon;
            this.maxAqi = maxAqi;
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

        public String getTextColourHex() {
            return textColourFor(colourHex);
        }

        public IIcon getIcon() {
            return icon;
        }

        /**
         * Inclusive upper bound of this band.
         */
        public double getMaxAqi() {
            return maxAqi;
        }
    }
}
