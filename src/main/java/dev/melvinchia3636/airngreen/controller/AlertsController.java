package dev.melvinchia3636.airngreen.controller;

import javax.swing.SwingUtilities;

import dev.melvinchia3636.airngreen.model.AlertThreshold;
import dev.melvinchia3636.airngreen.service.AirQualityService;
import dev.melvinchia3636.airngreen.service.SettingsStore;
import dev.melvinchia3636.airngreen.service.alerts.AlertLogObserver;
import dev.melvinchia3636.airngreen.ui.AlertsPanel;
import dev.melvinchia3636.airngreen.ui.StatusBar;

/**
 * Controller for alert settings, history and the service probe.
 */
public final class AlertsController {

    private final AlertsPanel view;
    private final SettingsStore settings;
    private final AirQualityService airQuality;
    private final AlertLogObserver history;
    private final StatusBar status;

    /**
     * Creates the controller.
     *
     * @param view       alerts view
     * @param settings   saved preferences
     * @param airQuality service used for the liveness probe
     * @param history    observer that records delivered alerts
     * @param status     window status line
     */
    public AlertsController(AlertsPanel view, SettingsStore settings,
                            AirQualityService airQuality,
                            AlertLogObserver history, StatusBar status) {
        this.view = view;
        this.settings = settings;
        this.airQuality = airQuality;
        this.history = history;
        this.status = status;
    }

    /**
     * Wires the view and renders any history already collected.
     */
    public void start() {
        view.onThresholdChange(this::changeThreshold);
        view.onCheckService(this::checkService);
        history.onAlertRecorded(alert -> SwingUtilities.invokeLater(
                () -> view.showHistory(history.alerts())));
        view.showHistory(history.alerts());
    }

    private void changeThreshold(AlertThreshold threshold) {
        settings.threshold(threshold);
        status.show(threshold.isEnabled()
                ? "Alerts enabled from " + threshold + "."
                : "Alerts disabled.");
    }

    private void checkService() {
        view.setChecking(true);
        Async.run(airQuality::isServiceAlive,
                alive -> {
                    view.setChecking(false);
                    view.showServiceStatus(alive);
                    status.show(alive ? "Service is online." : "Service is offline.");
                },
                failure -> {
                    view.setChecking(false);
                    view.showServiceStatus(false);
                    status.show("Service check failed.");
                });
    }
}
