package dev.melvinchia3636.airngreen.service.alerts;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import dev.melvinchia3636.airngreen.model.AqiAlert;

/**
 * Observer that keeps the alerts shown on the Alerts screen.
 *
 * <p>The view cannot observe a plain list, so the log also exposes a change
 * callback. Listeners run on the thread that delivers the alert.</p>
 */
public final class AlertLogObserver implements AlertObserver {

    private final List<AqiAlert> alerts = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Consumer<AqiAlert>> listeners =
            new CopyOnWriteArrayList<>();

    @Override
    public void onThresholdExceeded(AqiAlert alert) {
        alerts.add(0, alert);
        for (Consumer<AqiAlert> listener : listeners) {
            listener.accept(alert);
        }
    }

    /**
     * Registers a callback invoked after each recorded alert.
     *
     * @param listener callback receiving the new alert
     */
    public void onAlertRecorded(Consumer<AqiAlert> listener) {
        listeners.addIfAbsent(listener);
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
