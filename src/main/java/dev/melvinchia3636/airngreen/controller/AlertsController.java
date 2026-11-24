package dev.melvinchia3636.airngreen.controller;

import dev.melvinchia3636.airngreen.model.AlertThreshold;
import dev.melvinchia3636.airngreen.service.alerts.AlertLogObserver;
import dev.melvinchia3636.airngreen.storage.SettingsStore;
import dev.melvinchia3636.airngreen.ui.AlertsPanel;
import dev.melvinchia3636.airngreen.ui.StatusBar;

/** Controller for alert settings and history. */
public final class AlertsController {

    private final AlertsPanel alertsPanel;
    private final SettingsStore settingsStore;
    private final AlertLogObserver alertLogObserver;
    private final StatusBar statusBar;

    /**
     * Creates the controller.
     *
     * @param alertsPanel alerts view
     * @param settingsStore saved preferences
     * @param alertLogObserver observer that records delivered alerts
     * @param statusBar window status line
     */
    public AlertsController(
        AlertsPanel alertsPanel,
        SettingsStore settingsStore,
        AlertLogObserver alertLogObserver,
        StatusBar statusBar
    ) {
        this.alertsPanel = alertsPanel;
        this.settingsStore = settingsStore;
        this.alertLogObserver = alertLogObserver;
        this.statusBar = statusBar;
    }

    /** Wires the view and renders any history already collected. */
    public void start() {
        alertsPanel.onThresholdChange(this::changeThreshold);
        alertLogObserver.onAlertRecorded(
            alert -> alertsPanel.showHistory(alertLogObserver.getAlerts())
        );
        alertsPanel.showHistory(alertLogObserver.getAlerts());
    }

    private void changeThreshold(AlertThreshold threshold) {
        settingsStore.setThreshold(threshold);
        statusBar.show(
            threshold.isEnabled()
                ? "Alerts enabled from " + threshold + "."
                : "Alerts disabled."
        );
    }
}
