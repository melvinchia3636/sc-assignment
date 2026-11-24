package dev.melvinchia3636.airngreen.controller;

import java.util.List;

import dev.melvinchia3636.airngreen.model.StationSummary;
import dev.melvinchia3636.airngreen.service.AirQualityService;
import dev.melvinchia3636.airngreen.ui.StationsPanel;
import dev.melvinchia3636.airngreen.ui.StatusBar;

/**
 * Controller for the station directory.
 */
public final class StationsController {

    private final StationsPanel view;
    private final AirQualityService airQuality;
    private final StatusBar status;

    /**
     * Creates the controller.
     *
     * @param view       station directory view
     * @param airQuality reading source
     * @param status     window status line
     */
    public StationsController(StationsPanel view, AirQualityService airQuality,
                              StatusBar status) {
        this.view = view;
        this.airQuality = airQuality;
        this.status = status;
    }

    /**
     * Wires the view and shows a cached list when one exists.
     */
    public void start() {
        view.onReload(this::loadAll);
        view.onLoadSelected(this::open);

        List<StationSummary> cached = airQuality.cachedStations();
        if (!cached.isEmpty()) {
            view.setStations(cached);
            view.showMessage("Showing the cached station list. Reload for fresh data.",
                    false);
        }
    }

    private void loadAll() {
        view.setLoading(true);
        view.showMessage("Loading stations...", false);
        status.show("Loading the station directory...");

        Async.run(airQuality::stations,
                stations -> {
                    view.setLoading(false);
                    view.setStations(stations);
                    view.showMessage("Loaded " + stations.size() + " stations.", false);
                    status.show("Station directory loaded.");
                },
                failure -> {
                    view.setLoading(false);
                    view.showMessage(failure.getMessage(), true);
                    status.show("Could not load stations.");
                });
    }

    private void open(String idx) {
        status.show("Loading station " + idx + "...");
        Async.run(() -> airQuality.byIdx(idx),
                reading -> {
                    view.showDetail(reading);
                    status.show("Loaded station " + reading.idx() + ".");
                },
                failure -> {
                    view.showMessage(failure.getMessage(), true);
                    status.show("Could not load station " + idx + ".");
                });
    }
}
