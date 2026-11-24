package dev.melvinchia3636.airngreen.service.alerts;

import java.util.ArrayList;
import java.util.List;

import dev.melvinchia3636.airngreen.model.AqiAlert;
import dev.melvinchia3636.airngreen.storage.LocalStore;

/**
 * Observer that archives alerts so the history survives a restart.
 *
 * <p>The newest alert is stored first and the archive is capped so the file
 * cannot grow without bound.</p>
 */
public final class AlertArchiveObserver implements AlertObserver {

    private static final int MAX_ARCHIVED_ALERTS = 50;

    private final LocalStore store;

    /**
     * Creates the observer.
     *
     * @param store persistence used for {@code alerts.json}
     */
    public AlertArchiveObserver(LocalStore store) {
        this.store = store;
    }

    @Override
    public void onThresholdExceeded(AqiAlert alert) {
        List<AqiAlert> archive = new ArrayList<>(store.loadAlerts());
        archive.add(0, alert);
        store.saveAlerts(archive.stream()
                .limit(MAX_ARCHIVED_ALERTS)
                .toList());
    }
}
