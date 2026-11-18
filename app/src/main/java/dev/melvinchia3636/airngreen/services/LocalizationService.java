package dev.melvinchia3636.airngreen.services;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import dev.melvinchia3636.airngreen.repositories.options.Language;

import java.util.Locale;

/**
 * Owns the current language selection and locale switching.
 */
public final class LocalizationService extends ChoiceService<Language> {

    public static final LocalizationService INSTANCE = new LocalizationService();

    private LocalizationService() {
    }

    @Override
    public Language[] options() {
        return Language.values();
    }

    @Override
    public Language current(Context context) {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        if (locales.isEmpty()) {
            return Language.SYSTEM;
        }
        Locale locale = locales.get(0);
        if (locale != null) {
            for (Language option : Language.values()) {
                if (matches(option, locale)) {
                    return option;
                }
            }
        }
        return Language.ENGLISH;
    }

    @Override
    public void apply(Context context, Language language) {
        if (language == current(context)) {
            return;
        }
        AppCompatDelegate.setApplicationLocales(localesFor(language));
    }

    @Override
    public int labelRes(Language value) {
        return value.getLabelRes();
    }

    private static boolean matches(Language language, Locale locale) {
        String tag = language.tag();
        return !tag.isEmpty() && tag.startsWith(locale.getLanguage());
    }

    private static LocaleListCompat localesFor(Language language) {
        String tag = language.tag();
        return tag.isEmpty()
                ? LocaleListCompat.getEmptyLocaleList()
                : LocaleListCompat.forLanguageTags(tag);
    }
}
