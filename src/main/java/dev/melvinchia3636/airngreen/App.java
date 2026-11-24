package dev.melvinchia3636.airngreen;

import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLightLaf;

import dev.melvinchia3636.airngreen.api.AqiApiClient;
import dev.melvinchia3636.airngreen.controller.AlertsController;
import dev.melvinchia3636.airngreen.controller.DashboardController;
import dev.melvinchia3636.airngreen.controller.ReportsController;
import dev.melvinchia3636.airngreen.controller.StationsController;
import dev.melvinchia3636.airngreen.service.AirQualityService;
import dev.melvinchia3636.airngreen.service.ParksService;
import dev.melvinchia3636.airngreen.service.ReportService;
import dev.melvinchia3636.airngreen.service.alerts.AlertArchiveObserver;
import dev.melvinchia3636.airngreen.service.alerts.AlertLogObserver;
import dev.melvinchia3636.airngreen.service.alerts.BannerObserver;
import dev.melvinchia3636.airngreen.service.alerts.PollutionAlertService;
import dev.melvinchia3636.airngreen.storage.LocalStore;
import dev.melvinchia3636.airngreen.storage.SettingsStore;
import dev.melvinchia3636.airngreen.ui.AlertsPanel;
import dev.melvinchia3636.airngreen.ui.DashboardPanel;
import dev.melvinchia3636.airngreen.ui.MainFrame;
import dev.melvinchia3636.airngreen.ui.ReportsPanel;
import dev.melvinchia3636.airngreen.ui.StationsPanel;
import dev.melvinchia3636.airngreen.ui.StatusBar;

/**
 * Composition root: builds services, views and controllers, then shows the
 * window.
 */
public final class App {

    private App() {
    }

    /**
     * Starts the desktop application.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(App::start);
    }

    private static void start() {
        LocalStore localStore = new LocalStore();
        SettingsStore settingsStore = new SettingsStore(localStore);
        String baseUrl = System.getProperty("airngreen.apiBase",
                AqiApiClient.DEFAULT_BASE_URL);
        AirQualityService airQualityService = new AirQualityService(
                new AqiApiClient(baseUrl), localStore);
        ReportService reportService = new ReportService(localStore);
        ParksService parksService = ParksService.fromClasspathOrEmpty();

        StatusBar statusBar = new StatusBar();
        AlertLogObserver alertHistory =
                new AlertLogObserver(localStore.loadAlerts());
        PollutionAlertService alertService =
                new PollutionAlertService(settingsStore::getThreshold);
        alertService.register(alertHistory);
        alertService.register(new AlertArchiveObserver(localStore));
        alertService.register(new BannerObserver(statusBar::showAlert));

        DashboardPanel dashboardPanel = new DashboardPanel(
                settingsStore.getDistrict(), settingsStore.getAudienceStrategy());
        StationsPanel stationsPanel = new StationsPanel();
        ReportsPanel reportsPanel = new ReportsPanel();
        AlertsPanel alertsPanel = new AlertsPanel(settingsStore.getThreshold());

        new DashboardController(dashboardPanel, airQualityService,
                parksService, settingsStore, alertService, statusBar).start();
        new StationsController(stationsPanel, airQualityService,
                settingsStore, statusBar).start();
        new ReportsController(reportsPanel, reportService, statusBar).start();
        new AlertsController(alertsPanel, settingsStore, airQualityService,
                alertHistory, statusBar).start();

        new MainFrame(dashboardPanel, stationsPanel, reportsPanel, alertsPanel,
                statusBar).setVisible(true);
    }
}
