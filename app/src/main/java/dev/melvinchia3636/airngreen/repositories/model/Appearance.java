package dev.melvinchia3636.airngreen.repositories.model;

import androidx.annotation.StringRes;

import dev.melvinchia3636.airngreen.R;

/**
 * Appearance modes offered by the in-app theme switcher.
 */
public enum Appearance {

    SYSTEM(R.string.appearance_system),
    LIGHT(R.string.appearance_light),
    DARK(R.string.appearance_dark);

    @StringRes
    private final int labelRes;

    Appearance(@StringRes int labelRes) {
        this.labelRes = labelRes;
    }

    @StringRes
    public int labelRes() {
        return labelRes;
    }
}
