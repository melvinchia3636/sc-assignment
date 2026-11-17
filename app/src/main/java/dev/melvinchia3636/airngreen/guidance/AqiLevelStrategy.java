package dev.melvinchia3636.airngreen.guidance;

import androidx.annotation.StringRes;

import com.mikepenz.iconics.typeface.IIcon;

/**
 * Strategy for one AQI band.
 */
public interface AqiLevelStrategy {

    Data data();

    default int labelRes() {
        return data().labelRes();
    }

    default int adviceRes() {
        return data().adviceRes();
    }

    default String colourHex() {
        return data().colourHex();
    }

    default IIcon icon() {
        return data().icon();
    }

    default double maxAqi() {
        return data().maxAqi();
    }

    default boolean matches(double aqi) {
        return !Double.isNaN(aqi) && aqi <= maxAqi();
    }

    record Data(@StringRes int labelRes, @StringRes int adviceRes,
                String colourHex, IIcon icon, double maxAqi) {
    }
}
