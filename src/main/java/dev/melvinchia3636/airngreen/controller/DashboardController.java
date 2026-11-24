package dev.melvinchia3636.airngreen.controller;

import java.util.Optional;

import dev.melvinchia3636.airngreen.model.District;
import dev.melvinchia3636.airngreen.model.StationReading;
import dev.melvinchia3636.airngreen.service.AirQualityService;
import dev.melvinchia3636.airngreen.service.ParksService;
import dev.melvinchia3636.airngreen.service.SettingsStore;
import dev.melvinchia3636.airngreen.service.advice.AdviceStrategy;
import dev.melvinchia3636.airngreen.service.alerts.PollutionAlertService;
import dev.melvinchia3636.airngreen.ui.DashboardPanel;
import dev.melvinchia3636.airngreen.ui.StatusBar;

/**
 * Controller for the dashboard: loads readings and feeds them to the alert
 * subject.
 */
public final class DashboardController {

    private final DashboardPanel view;
    private final AirQualityService airQuality;
    private final ParksService parks;
    private final SettingsStore settings;
    private final PollutionAlertService alerts;
    private final StatusBar status;

    private StationReading current;

    /**
     * Creates the controller.
     *
     * @param view       dashboard view
     * @param airQuality reading source
     * @param parks      park recommendations
     * @param settings   saved preferences
     * @param alerts     observer subject
     * @param status     window status line
     */
    public DashboardController(DashboardPanel view, AirQualityService airQuality,
                               ParksService parks, SettingsStore settings,
                               PollutionAlertService alerts, StatusBar status) {
        this.view = view;
        this.airQuality = airQuality;
        this.parks = parks;
        this.settings = settings;
        this.alerts = alerts;
        this.status = status;
    }

    /**
     * Wires the view and performs the first load.
     */
    public void start() {
        view.onRefresh(this::refresh);
        view.onDistrictChange(this::changeDistrict);
        view.onAudienceChange(this::changeAudience);
        refresh();
    }

    private void changeDistrict(District district) {
        settings.district(district);
        refresh();
    }

    private void changeAudience(AdviceStrategy advice) {
        settings.advice(advice);
        if (current != null) {
            render(current);
        }
    }

    private void refresh() {
        District district = view.selectedDistrict();
        view.setLoading(true);
        view.clearMessage();
        status.show("Loading air quality for " + district.label() + "...");

        Async.run(() -> airQuality.nearest(district),
                reading -> onReading(district, reading),
                failure -> onFailure(district, failure));
    }

    private void onReading(District district, StationReading reading) {
        view.setLoading(false);
        current = reading;
        render(reading);
        alerts.evaluate(reading);
        status.show("Loaded " + district.label() + " at "
                + reading.level().label() + ".");
    }

    private void onFailure(District district, Exception failure) {
        view.setLoading(false);
        view.showParks(parks.nearest(district));
        Optional<StationReading> cached = airQuality.cachedReading();
        if (cached.isPresent()) {
            current = cached.get();
            render(current);
            view.showMessage("Offline: showing the last saved reading. "
                    + failure.getMessage(), true);
        } else {
            view.showMessage(failure.getMessage(), true);
        }
        status.show("Could not refresh: " + failure.getMessage());
    }

    private void render(StationReading reading) {
        AdviceStrategy advice = settings.advice();
        view.showReading(reading,
                advice.advice(reading.level()),
                advice.outdoorMinutes(reading.level()));
        view.showParks(parks.nearest(view.selectedDistrict()));
    }
}
