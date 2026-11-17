package com.groupone.assignment.ui.settings;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.groupone.assignment.R;

import java.util.Locale;

/**
 * The languages offered by the in-app switcher. Each entry owns its label and
 * locale tag, so callers never need position or tag literals.
 */
public enum AppLanguage {

    SYSTEM(R.string.language_system, ""),
    ENGLISH(R.string.language_english, "en"),
    MALAY(R.string.language_malay, "ms"),
    CHINESE(R.string.language_chinese, "zh-CN");

    @StringRes
    private final int labelRes;
    private final String tag;

    AppLanguage(@StringRes int labelRes, String tag) {
        this.labelRes = labelRes;
        this.tag = tag;
    }

    @StringRes
    public int getLabelRes() {
        return labelRes;
    }

    public LocaleListCompat locales() {
        return tag.isEmpty()
                ? LocaleListCompat.getEmptyLocaleList()
                : LocaleListCompat.forLanguageTags(tag);
    }

    public static AppLanguage current() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        if (locales.isEmpty()) {
            return SYSTEM;
        }
        Locale locale = locales.get(0);
        String language = locale == null ? "" : locale.getLanguage();
        for (AppLanguage languageOption : values()) {
            if (!languageOption.tag.isEmpty() && languageOption.tag.startsWith(language)) {
                return languageOption;
            }
        }
        return ENGLISH;
    }
}
