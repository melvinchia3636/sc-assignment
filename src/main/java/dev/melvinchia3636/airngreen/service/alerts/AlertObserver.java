package dev.melvinchia3636.airngreen.service.alerts;

/**
 * Observer contract for pollution alerts.
 */
public interface AlertObserver {

    /**
     * Called when a reading crosses the configured threshold.
     *
     * @param alert details of the crossing
     */
    void onThresholdExceeded(AqiAlert alert);
}
