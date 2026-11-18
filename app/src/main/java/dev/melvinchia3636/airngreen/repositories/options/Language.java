package dev.melvinchia3636.airngreen.repositories.options;

import androidx.annotation.StringRes;

import dev.melvinchia3636.airngreen.R;

/**
 * Languages offered by the in-app switcher. The empty tag defers to the system.
 */
public enum Language {

    SYSTEM(R.string.language_system, ""),
    ENGLISH(R.string.language_english, "en"),
    MALAY(R.string.language_malay, "ms"),
    CHINESE(R.string.language_chinese, "zh-CN"),
    CHINESE_TRADITIONAL(R.string.language_chinese_traditional, "zh-TW"),
    JAPANESE(R.string.language_japanese, "ja"),
    KOREAN(R.string.language_korean, "ko"),
    HINDI(R.string.language_hindi, "hi"),
    FRENCH(R.string.language_french, "fr"),
    GERMAN(R.string.language_german, "de"),
    ITALIAN(R.string.language_italian, "it"),
    THAI(R.string.language_thai, "th"),
    POLISH(R.string.language_polish, "pl"),
    RUSSIAN(R.string.language_russian, "ru"),
    SPANISH(R.string.language_spanish, "es"),
    SWEDISH(R.string.language_swedish, "sv"),
    TAMIL(R.string.language_tamil, "ta"),
    TURKISH(R.string.language_turkish, "tr");

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

    /** BCP 47 language tag, or an empty string to follow the system. */
    public String tag() {
        return tag;
    }
}
