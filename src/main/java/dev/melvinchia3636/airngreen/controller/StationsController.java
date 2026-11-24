package dev.melvinchia3636.airngreen.controller;

import java.util.List;

import dev.melvinchia3636.airngreen.model.District;
import dev.melvinchia3636.airngreen.model.StationReading;
import dev.melvinchia3636.airngreen.model.StationSummary;
import dev.melvinchia3636.airngreen.service.AirQualityService;
import dev.melvinchia3636.airngreen.storage.SettingsStore;
import dev.melvinchia3636.airngreen.ui.StationsPanel;
import dev.melvinchia3636.airngreen.ui.StatusBar;

/**
 * Controller for the station directory.
 */
public final class StationsController {

    private final StationsPanel stationsPanel;
    private final AirQualityService airQualityService;
    private final SettingsStore settingsStore;
    private final StatusBar statusBar;

    /**
     * Creates the controller.
     *
     * @param stationsPanel    station directory view
     * @param airQualityService reading source
     * @param settingsStore    saved preferences, used for the district
     * @param statusBar        window status line
     */
    public StationsController(StationsPanel stationsPanel,
                              AirQualityService airQualityService,
                              SettingsStore settingsStore,
                              StatusBar statusBar) {
        this.stationsPanel = stationsPanel;
        this.airQualityService = airQualityService;
        this.settingsStore = settingsStore;
        this.statusBar = statusBar;
    }

    /**
     * Wires the view and refreshes the directory from the service.
     */
    public void start() {
        stationsPanel.onReload(this::loadStations);
        stationsPanel.onLoadSelected(this::loadStationDetail);
        settingsStore.onDistrictChange(district -> loadStations());
        stationsPanel.setDistrict(settingsStore.getDistrict());
        loadStations();
    }

    private void loadStations() {
        District district = settingsStore.getDistrict();
        stationsPanel.setLoading(true);
        stationsPanel.showMessage(
                "Loading stations near " + district.label() + "...", false);
        statusBar.show("Loading stations near " + district.label() + "...");

        Async.run(() -> airQualityService.stations(district),
                stations -> onStationsLoaded(district, stations),
                this::onStationsFailed);
    }

    private void onStationsLoaded(District district,
                                  List<StationSummary> stations) {
        stationsPanel.setLoading(false);
        stationsPanel.setDistrict(district);
        stationsPanel.setStations(stations);
        stationsPanel.showMessage(
                "Loaded " + stations.size() + " stations.", false);
        statusBar.show("Station directory loaded.");
    }

    private void onStationsFailed(Exception failure) {
        stationsPanel.setLoading(false);

        List<StationSummary> cachedStations =
                airQualityService.getCachedStations();
        if (cachedStations.isEmpty()) {
            stationsPanel.showMessage(failure.getMessage(), true);
        } else {
            stationsPanel.setDistrict(settingsStore.getDistrict());
            stationsPanel.setStations(cachedStations);
            stationsPanel.showMessage(
                    "Offline: showing cached stations. "
                            + failure.getMessage(), true);
        }
        statusBar.show("Could not load stations.");
    }

    private void loadStationDetail(String idx) {
        stationsPanel.showDetailLoading(idx);
        stationsPanel.setDetailLoading(true);
        statusBar.show("Loading station " + idx + "...");

        Async.run(() -> airQualityService.byIdx(idx),
                this::onStationDetailLoaded,
                failure -> onStationDetailFailed(idx, failure));
    }

    private void onStationDetailLoaded(StationReading reading) {
        stationsPanel.setDetailLoading(false);
        stationsPanel.showDetail(reading);
        statusBar.show("Loaded station " + reading.idx() + ".");
    }

    private void onStationDetailFailed(String idx, Exception failure) {
        stationsPanel.setDetailLoading(false);
        stationsPanel.showDetailError(failure.getMessage());
        stationsPanel.showMessage(failure.getMessage(), true);
        statusBar.show("Could not load station " + idx + ".");
    }
}
