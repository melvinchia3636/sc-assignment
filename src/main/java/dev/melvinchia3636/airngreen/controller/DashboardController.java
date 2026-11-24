package dev.melvinchia3636.airngreen.controller;

import java.util.Optional;

import dev.melvinchia3636.airngreen.api.ApiException;
import dev.melvinchia3636.airngreen.model.District;
import dev.melvinchia3636.airngreen.model.StationReading;
import dev.melvinchia3636.airngreen.service.AirQualityService;
import dev.melvinchia3636.airngreen.service.ParksService;
import dev.melvinchia3636.airngreen.storage.SettingsStore;
import dev.melvinchia3636.airngreen.service.advice.AdviceStrategy;
import dev.melvinchia3636.airngreen.service.alerts.PollutionAlertService;
import dev.melvinchia3636.airngreen.ui.DashboardPanel;
import dev.melvinchia3636.airngreen.ui.StatusBar;

/**
 * Controller for the dashboard: loads readings and feeds them to the alert
 * subject.
 */
public final class DashboardController {

    private final DashboardPanel dashboardPanel;
    private final AirQualityService airQualityService;
    private final ParksService parksService;
    private final SettingsStore settingsStore;
    private final PollutionAlertService pollutionAlertService;
    private final StatusBar statusBar;

    private StationReading currentReading;

    /**
     * Creates the controller.
     *
     * @param dashboardPanel       dashboard view
     * @param airQualityService    reading source
     * @param parksService         park recommendations
     * @param settingsStore        saved preferences
     * @param pollutionAlertService observer subject
     * @param statusBar            window status line
     */
    public DashboardController(DashboardPanel dashboardPanel,
                               AirQualityService airQualityService,
                               ParksService parksService,
                               SettingsStore settingsStore,
                               PollutionAlertService pollutionAlertService,
                               StatusBar statusBar) {
        this.dashboardPanel = dashboardPanel;
        this.airQualityService = airQualityService;
        this.parksService = parksService;
        this.settingsStore = settingsStore;
        this.pollutionAlertService = pollutionAlertService;
        this.statusBar = statusBar;
    }

    /**
     * Wires the view and performs the first load.
     */
    public void start() {
        dashboardPanel.onRefresh(this::refresh);
        dashboardPanel.onDistrictChange(this::changeDistrict);
        dashboardPanel.onAudienceChange(this::changeAudience);
        refresh();
    }

    private void changeDistrict(District district) {
        settingsStore.setDistrict(district);
        refresh();
    }

    private void changeAudience(AdviceStrategy strategy) {
        settingsStore.setAudienceStrategy(strategy);
        if (currentReading != null) {
            render(currentReading);
        }
    }

    private void refresh() {
        District district = dashboardPanel.getSelectedDistrict();
        dashboardPanel.setLoading(true);
        dashboardPanel.clearMessage();
        statusBar.show("Loading air quality for " + district.label() + "...");

        Async.run(() -> airQualityService.nearest(district),
                reading -> onReading(district, reading),
                failure -> onFailure(district, failure));
    }

    private void onReading(District district, StationReading reading) {
        dashboardPanel.setLoading(false);
        currentReading = reading;
        render(reading);
        pollutionAlertService.evaluate(reading);
        statusBar.show("Loaded " + district.label() + " at "
                + reading.level().label() + ".");
    }

    private void onFailure(District district, Exception failure) {
        dashboardPanel.setLoading(false);
        dashboardPanel.showParks(parksService.nearest(district));

        // A client error (for example an unknown station) should not be hidden
        // behind stale data; only a service or network failure falls back.
        Optional<StationReading> cached = failure instanceof ApiException api
                && !api.isServiceFailure()
                ? Optional.empty()
                : airQualityService.getCachedReading();

        if (cached.isPresent()) {
            currentReading = cached.get();
            render(currentReading);
            dashboardPanel.showMessage("Offline: showing the last saved reading. "
                    + failure.getMessage(), true);
        } else {
            dashboardPanel.showMessage(failure.getMessage(), true);
        }
        statusBar.show("Could not refresh: " + failure.getMessage());
    }

    private void render(StationReading reading) {
        AdviceStrategy strategy = settingsStore.getAudienceStrategy();
        dashboardPanel.showReading(reading,
                strategy.advice(reading.level()),
                strategy.outdoorMinutes(reading.level()));
        dashboardPanel.showParks(
                parksService.nearest(dashboardPanel.getSelectedDistrict()));
    }
}
