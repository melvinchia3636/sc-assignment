package dev.melvinchia3636.airngreen;

import java.io.IOException;
import java.util.List;

import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLightLaf;

import dev.melvinchia3636.airngreen.api.AqiApiClient;
import dev.melvinchia3636.airngreen.api.JdkHttpTransport;
import dev.melvinchia3636.airngreen.controller.AlertsController;
import dev.melvinchia3636.airngreen.controller.DashboardController;
import dev.melvinchia3636.airngreen.controller.ReportsController;
import dev.melvinchia3636.airngreen.controller.StationsController;
import dev.melvinchia3636.airngreen.service.AirQualityService;
import dev.melvinchia3636.airngreen.service.LocalStore;
import dev.melvinchia3636.airngreen.service.ParksService;
import dev.melvinchia3636.airngreen.service.ReportService;
import dev.melvinchia3636.airngreen.service.SettingsStore;
import dev.melvinchia3636.airngreen.service.alerts.AlertLogObserver;
import dev.melvinchia3636.airngreen.service.alerts.BannerObserver;
import dev.melvinchia3636.airngreen.service.alerts.PollutionAlertService;
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
        LocalStore store = new LocalStore();
        SettingsStore settings = new SettingsStore(store);
        String baseUrl = System.getProperty("airngreen.apiBase",
                AqiApiClient.DEFAULT_BASE_URL);
        AirQualityService airQuality = new AirQualityService(
                new AqiApiClient(new JdkHttpTransport(), baseUrl), store);
        ReportService reports = new ReportService(store);
        ParksService parks = loadParks();

        StatusBar status = new StatusBar();
        AlertLogObserver history = new AlertLogObserver();
        PollutionAlertService alerts =
                new PollutionAlertService(settings::threshold);
        alerts.register(history);
        alerts.register(new BannerObserver(status::showAlert));

        DashboardPanel dashboard =
                new DashboardPanel(settings.district(), settings.advice());
        StationsPanel stations = new StationsPanel();
        ReportsPanel reportPanel = new ReportsPanel();
        AlertsPanel alertsPanel = new AlertsPanel(settings.threshold());

        new DashboardController(dashboard, airQuality, parks, settings, alerts, status)
                .start();
        new StationsController(stations, airQuality, status).start();
        new ReportsController(reportPanel, reports, status).start();
        new AlertsController(alertsPanel, settings, airQuality, history, status).start();

        new MainFrame(dashboard, stations, reportPanel, alertsPanel, status)
                .setVisible(true);
    }

    private static ParksService loadParks() {
        try {
            return ParksService.fromClasspath();
        } catch (IOException missing) {
            System.err.println("Park dataset unavailable: " + missing.getMessage());
            return new ParksService(List.of());
        }
    }
}
