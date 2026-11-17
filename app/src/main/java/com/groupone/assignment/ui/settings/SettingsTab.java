package com.groupone.assignment.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.groupone.assignment.R;
import com.groupone.assignment.databinding.TabSettingsBinding;
import com.groupone.assignment.ui.common.BaseTab;

/**
 * Settings screen: data-source attribution and the in-app language switcher.
 */
public class SettingsTab extends BaseTab<TabSettingsBinding> {

    @Override
    protected TabSettingsBinding createBinding(LayoutInflater inflater,
                                                    @Nullable ViewGroup container) {
        return TabSettingsBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        syncSelection();
        binding.languageCard.setOnClickListener(clicked -> showLanguageDialog());
    }

    private void syncSelection() {
        binding.languageValue.setText(AppLanguage.current().getLabelRes());
    }

    private void showLanguageDialog() {
        AppLanguage[] languages = AppLanguage.values();
        String[] labels = new String[languages.length];
        for (int i = 0; i < languages.length; i++) {
            labels[i] = getString(languages[i].getLabelRes());
        }

        new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_AirGreen_Dialog)
                .setTitle(R.string.settings_language)
                .setSingleChoiceItems(labels, AppLanguage.current().ordinal(),
                        (dialog, which) -> {
                            dialog.dismiss();
                            applyLanguage(languages[which].locales());
                        })
                .show();
    }

    private void applyLanguage(LocaleListCompat locales) {
        LocaleListCompat current = AppCompatDelegate.getApplicationLocales();
        if (locales.toLanguageTags().equals(current.toLanguageTags())) {
            return;
        }
        AppCompatDelegate.setApplicationLocales(locales);
    }
}
