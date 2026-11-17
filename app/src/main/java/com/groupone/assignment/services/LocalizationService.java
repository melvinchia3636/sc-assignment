package com.groupone.assignment.services;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.groupone.assignment.R;

import java.util.Locale;

/**
 * Owns the in-app language options, current selection and locale switching.
 */
public final class LocalizationService {

    public enum Language {
        SYSTEM(R.string.language_system, ""),
        ENGLISH(R.string.language_english, "en"),
        MALAY(R.string.language_malay, "ms"),
        CHINESE(R.string.language_chinese, "zh-CN");

        @StringRes
        private final int labelRes;
        private final String tag;

        Language(@StringRes int labelRes, String tag) {
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
    }

    private LocalizationService() {
    }

    public static Language current() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        if (locales.isEmpty()) {
            return Language.SYSTEM;
        }
        Locale locale = locales.get(0);
        String language = locale == null ? "" : locale.getLanguage();
        for (Language option : Language.values()) {
            if (!option.tag.isEmpty() && option.tag.startsWith(language)) {
                return option;
            }
        }
        return Language.ENGLISH;
    }

    public static void apply(Language language) {
        if (language == current()) {
            return;
        }
        AppCompatDelegate.setApplicationLocales(language.locales());
    }
}
