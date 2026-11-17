package dev.melvinchia3636.airngreen.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import dev.melvinchia3636.airngreen.R;
import dev.melvinchia3636.airngreen.databinding.TabSettingsBinding;
import dev.melvinchia3636.airngreen.services.AudienceService;
import dev.melvinchia3636.airngreen.services.ChoiceService;
import dev.melvinchia3636.airngreen.services.LocalizationService;
import dev.melvinchia3636.airngreen.ui.common.BaseTab;
import dev.melvinchia3636.airngreen.ui.common.SingleChoiceDialog;
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
        binding.languageRow.setValue(
                LocalizationService.INSTANCE.labelRes(
                        LocalizationService.INSTANCE.current(requireContext())));
        binding.audienceRow.setValue(
                AudienceService.INSTANCE.labelRes(
                        AudienceService.INSTANCE.current(requireContext())));

        binding.languageRow.setOnClickListener(clicked -> showLanguageDialog());
        binding.audienceRow.setOnClickListener(clicked -> showAudienceDialog());
    }

    private void showLanguageDialog() {
        showChoiceDialog(R.string.settings_language, LocalizationService.INSTANCE, () -> {
            // Locale changes recreate the activity.
        });
    }

    private void showAudienceDialog() {
        showChoiceDialog(R.string.settings_audience, AudienceService.INSTANCE,
                () -> binding.audienceRow.setValue(
                        AudienceService.INSTANCE.labelRes(
                                AudienceService.INSTANCE.current(requireContext()))));
    }

    private <T> void showChoiceDialog(@StringRes int titleRes,
                                      ChoiceService<T> service,
                                      Runnable afterApply) {
        SingleChoiceDialog.show(
                requireContext(),
                titleRes,
                service.options(),
                service::labelRes,
                service.indexOf(service.current(requireContext())),
                value -> {
                    service.apply(requireContext(), value);
                    afterApply.run();
                });
    }
}
