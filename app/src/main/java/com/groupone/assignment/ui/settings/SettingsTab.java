package com.groupone.assignment.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.groupone.assignment.R;
import com.groupone.assignment.databinding.TabSettingsBinding;
import com.groupone.assignment.repositories.model.Audience;
import com.groupone.assignment.services.LocalizationService;
import com.groupone.assignment.settings.UserPreferences;
import com.groupone.assignment.ui.common.BaseTab;
import com.groupone.assignment.ui.common.SingleChoiceDialog;
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
        binding.languageRow.setValue(LocalizationService.current().getLabelRes());
        binding.audienceRow.setValue(
                UserPreferences.currentAudience(requireContext()).getLabelRes());

        binding.languageRow.setOnClickListener(clicked -> showLanguageDialog());
        binding.audienceRow.setOnClickListener(clicked -> showAudienceDialog());
    }

    private void showLanguageDialog() {
        SingleChoiceDialog.show(
                requireContext(),
                R.string.settings_language,
                LocalizationService.Language.values(),
                LocalizationService.Language::getLabelRes,
                LocalizationService.current().ordinal(),
                LocalizationService::apply);
    }

    private void showAudienceDialog() {
        SingleChoiceDialog.show(
                requireContext(),
                R.string.settings_audience,
                Audience.values(),
                Audience::getLabelRes,
                UserPreferences.currentAudience(requireContext()).ordinal(),
                audience -> {
                    UserPreferences.setAudience(requireContext(), audience);
                    binding.audienceRow.setValue(audience.getLabelRes());
                });
    }
}
