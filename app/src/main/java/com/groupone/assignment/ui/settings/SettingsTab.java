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
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;

/**
 * Settings screen: personalization and data-source attribution.
 */
public class SettingsTab extends BaseTab<TabSettingsBinding> {

    public static final GoogleMaterial.Icon NAV_ICON = GoogleMaterial.Icon.gmd_settings;

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
        binding.audienceCard.setOnClickListener(clicked -> showAudienceDialog());
    }

    private void syncSelection() {
        binding.languageValue.setText(AppLanguage.current().getLabelRes());
        binding.audienceValue.setText(AudienceSettings.current(requireContext()).getLabelRes());
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

    private void showAudienceDialog() {
        Audience[] audiences = Audience.values();
        String[] labels = new String[audiences.length];
        for (int i = 0; i < audiences.length; i++) {
            labels[i] = getString(audiences[i].getLabelRes());
        }

        new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_AirGreen_Dialog)
                .setTitle(R.string.settings_audience)
                .setSingleChoiceItems(labels,
                        AudienceSettings.current(requireContext()).ordinal(),
                        (dialog, which) -> {
                            dialog.dismiss();
                            AudienceSettings.set(requireContext(), audiences[which]);
                            syncSelection();
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
