package dev.melvinchia3636.airngreen.service.alerts;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import dev.melvinchia3636.airngreen.model.AqiAlert;

/**
 * Observer that keeps the alerts shown on the Alerts screen.
 */
public final class AlertLogObserver implements AlertObserver {

    private final List<AqiAlert> alerts = new CopyOnWriteArrayList<>();

    @Override
    public void onThresholdExceeded(AqiAlert alert) {
        alerts.add(0, alert);
    }

    /**
     * Alerts received so far, newest first.
     *
     * @return an immutable snapshot
     */
    public List<AqiAlert> alerts() {
        return List.copyOf(alerts);
    }
}
