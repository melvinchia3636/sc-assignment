package dev.melvinchia3636.airngreen.controller;

import javax.swing.SwingUtilities;

import dev.melvinchia3636.airngreen.model.AlertThreshold;
import dev.melvinchia3636.airngreen.storage.SettingsStore;
import dev.melvinchia3636.airngreen.service.alerts.AlertLogObserver;
import dev.melvinchia3636.airngreen.ui.AlertsPanel;
import dev.melvinchia3636.airngreen.ui.StatusBar;

/**
 * Controller for alert settings and history.
 */
public final class AlertsController {

    private final AlertsPanel view;
    private final SettingsStore settings;
    private final AlertLogObserver history;
    private final StatusBar status;

    /**
     * Creates the controller.
     *
     * @param view     alerts view
     * @param settings saved preferences
     * @param history  observer that records delivered alerts
     * @param status   window status line
     */
    public AlertsController(AlertsPanel view, SettingsStore settings,
                            AlertLogObserver history, StatusBar status) {
        this.view = view;
        this.settings = settings;
        this.history = history;
        this.status = status;
    }

    /**
     * Wires the view and renders any history already collected.
     */
    public void start() {
        view.onThresholdChange(this::changeThreshold);
        history.onAlertRecorded(alert -> SwingUtilities.invokeLater(
                () -> view.showHistory(history.getAlerts())));
        view.showHistory(history.getAlerts());
    }

    private void changeThreshold(AlertThreshold threshold) {
        settings.setThreshold(threshold);
        status.show(threshold.isEnabled()
                ? "Alerts enabled from " + threshold + "."
                : "Alerts disabled.");
    }
}
