package dev.melvinchia3636.airngreen.repositories.options;

import androidx.annotation.StringRes;
import androidx.core.os.LocaleListCompat;

import dev.melvinchia3636.airngreen.R;

import java.util.Locale;

/**
 * Languages offered by the in-app switcher.
 */
public enum Language {

    SYSTEM(R.string.language_system, ""),
    ENGLISH(R.string.language_english, "en"),
    MALAY(R.string.language_malay, "ms"),
    CHINESE(R.string.language_chinese, "zh-CN"),
    JAPANESE(R.string.language_japanese, "ja");

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

    public boolean matches(Locale locale) {
        return !tag.isEmpty() && tag.startsWith(locale.getLanguage());
    }
}
