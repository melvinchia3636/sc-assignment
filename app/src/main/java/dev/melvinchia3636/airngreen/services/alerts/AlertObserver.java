package dev.melvinchia3636.airngreen.services.alerts;

/**
 * Receives pollution alerts. Implementations must tolerate being called from a
 * background thread.
 */
public interface AlertObserver {

    void onThresholdExceeded(AqiAlert alert);
}
