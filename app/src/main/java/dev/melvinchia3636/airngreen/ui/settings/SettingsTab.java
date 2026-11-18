package dev.melvinchia3636.airngreen.ui.settings;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import dev.melvinchia3636.airngreen.R;
import dev.melvinchia3636.airngreen.databinding.TabSettingsBinding;
import dev.melvinchia3636.airngreen.repositories.options.AlertThreshold;
import dev.melvinchia3636.airngreen.repositories.options.MapProvider;
import dev.melvinchia3636.airngreen.services.AlertThresholdService;
import dev.melvinchia3636.airngreen.services.ApiKeyStore;
import dev.melvinchia3636.airngreen.services.AppearanceService;
import dev.melvinchia3636.airngreen.services.AudienceService;
import dev.melvinchia3636.airngreen.services.ChoiceService;
import dev.melvinchia3636.airngreen.services.LocalizationService;
import dev.melvinchia3636.airngreen.services.MapProviderService;
import dev.melvinchia3636.airngreen.ui.common.BaseTab;
import dev.melvinchia3636.airngreen.ui.common.SingleChoiceDialog;
import dev.melvinchia3636.airngreen.ui.common.TextInputDialog;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;

/**
 * Settings screen: personalization and data-source attribution.
 */
public class SettingsTab extends BaseTab<TabSettingsBinding> {

    public static final GoogleMaterial.Icon NAV_ICON = GoogleMaterial.Icon.gmd_settings;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    granted -> {
                    });

    @Override
    protected TabSettingsBinding createBinding(LayoutInflater inflater,
            @Nullable ViewGroup container) {
        return TabSettingsBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshValues();

        binding.languageRow.setOnClickListener(clicked -> showLanguageDialog());
        binding.appearanceRow.setOnClickListener(clicked -> showAppearanceDialog());
        binding.audienceRow.setOnClickListener(clicked -> showAudienceDialog());
        binding.mapProviderRow.setOnClickListener(clicked -> showMapProviderDialog());
        binding.mapKeyRow.setOnClickListener(clicked -> showGoogleKeyDialog());
        binding.alertThresholdRow.setOnClickListener(clicked -> showAlertDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) {
            refreshValues();
        }
    }

    private void refreshValues() {
        binding.languageRow.setValue(LocalizationService.INSTANCE.labelRes(
                LocalizationService.INSTANCE.current(requireContext())));
        binding.appearanceRow.setValue(AppearanceService.INSTANCE.labelRes(
                AppearanceService.INSTANCE.current(requireContext())));
        binding.audienceRow.setValue(AudienceService.INSTANCE.labelRes(
                AudienceService.INSTANCE.current(requireContext())));
        refreshMapValues();
        binding.alertThresholdRow.setValue(AlertThresholdService.INSTANCE.labelRes(
                AlertThresholdService.INSTANCE.current(requireContext())));
    }

    private void refreshMapValues() {
        MapProvider provider = MapProviderService.INSTANCE.current(requireContext());
        binding.mapProviderRow.setValue(MapProviderService.INSTANCE.labelRes(provider));
        binding.mapRow.setValue(MapProviderService.INSTANCE.labelRes(provider));
        binding.mapKeyRow.setValue(ApiKeyStore.hasGoogleMapsKey(requireContext())
                ? R.string.api_key_saved
                : R.string.api_key_missing);

        boolean google = provider == MapProvider.GOOGLE_MAPS;
        binding.mapKeyRow.setVisibility(google ? View.VISIBLE : View.GONE);
        binding.mapKeyDivider.setVisibility(google ? View.VISIBLE : View.GONE);
    }

    private void showLanguageDialog() {
        showChoiceDialog(R.string.settings_language, LocalizationService.INSTANCE, () -> {
            // Locale changes recreate the activity.
        });
    }

    private void showAppearanceDialog() {
        // The activity only recreates when the night mode actually changes.
        showChoiceDialog(R.string.settings_appearance, AppearanceService.INSTANCE,
                this::refreshValues);
    }

    private void showAudienceDialog() {
        showChoiceDialog(R.string.settings_audience, AudienceService.INSTANCE,
                this::refreshValues);
    }

    private void showMapProviderDialog() {
        showChoiceDialog(R.string.settings_map_provider, MapProviderService.INSTANCE,
                this::refreshMapValues);
    }

    private void showAlertDialog() {
        showChoiceDialog(R.string.settings_alert_threshold, AlertThresholdService.INSTANCE,
                () -> {
                    AlertThreshold threshold =
                            AlertThresholdService.INSTANCE.current(requireContext());
                    refreshValues();
                    if (threshold.isEnabled()) {
                        requestNotificationPermission();
                    }
                });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void showGoogleKeyDialog() {
        TextInputDialog.show(
                requireContext(),
                R.string.settings_google_key,
                R.string.api_key_hint,
                ApiKeyStore.googleMapsKey(requireContext()),
                key -> {
                    ApiKeyStore.saveGoogleMapsKey(requireContext(), key);
                    refreshMapValues();
                });
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
