package dev.melvinchia3636.airngreen.controller;

import java.util.List;

import dev.melvinchia3636.airngreen.model.District;
import dev.melvinchia3636.airngreen.model.StationSummary;
import dev.melvinchia3636.airngreen.service.AirQualityService;
import dev.melvinchia3636.airngreen.service.SettingsStore;
import dev.melvinchia3636.airngreen.ui.StationsPanel;
import dev.melvinchia3636.airngreen.ui.StatusBar;

/**
 * Controller for the station directory.
 */
public final class StationsController {

    private final StationsPanel view;
    private final AirQualityService airQuality;
    private final SettingsStore settings;
    private final StatusBar status;

    /**
     * Creates the controller.
     *
     * @param view       station directory view
     * @param airQuality reading source
     * @param settings   saved preferences, used for the district
     * @param status     window status line
     */
    public StationsController(StationsPanel view, AirQualityService airQuality,
                              SettingsStore settings, StatusBar status) {
        this.view = view;
        this.airQuality = airQuality;
        this.settings = settings;
        this.status = status;
    }

    /**
     * Wires the view and refreshes the directory from the service.
     */
    public void start() {
        view.onReload(this::loadAll);
        view.onLoadSelected(this::open);
        view.setDistrict(settings.district());
        loadAll();
    }

    private void loadAll() {
        District district = settings.district();
        view.setLoading(true);
        view.showMessage("Loading stations...", false);
        status.show("Loading stations including " + district.label() + "...");

        Async.run(() -> airQuality.stations(district),
                stations -> {
                    view.setLoading(false);
                    view.setDistrict(district);
                    view.setStations(stations);
                    view.showMessage("Loaded " + stations.size()
                            + " stations, including " + district.label() + ".",
                            false);
                    status.show("Station directory loaded.");
                },
                failure -> {
                    view.setLoading(false);
                    List<StationSummary> cached = airQuality.cachedStations();
                    if (!cached.isEmpty()) {
                        view.setDistrict(settings.district());
                        view.setStations(cached);
                        view.showMessage("Offline: showing cached stations. "
                                + failure.getMessage(), true);
                    } else {
                        view.showMessage(failure.getMessage(), true);
                    }
                    status.show("Could not load stations.");
                });
    }

    private void open(String idx) {
        view.showDetailLoading(idx);
        view.setDetailLoading(true);
        status.show("Loading station " + idx + "...");

        Async.run(() -> airQuality.byIdx(idx),
                reading -> {
                    view.setDetailLoading(false);
                    view.showDetail(reading);
                    status.show("Loaded station " + reading.idx() + ".");
                },
                failure -> {
                    view.setDetailLoading(false);
                    view.showDetailError(failure.getMessage());
                    view.showMessage(failure.getMessage(), true);
                    status.show("Could not load station " + idx + ".");
                });
    }
}
