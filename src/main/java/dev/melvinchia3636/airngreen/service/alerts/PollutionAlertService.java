package dev.melvinchia3636.airngreen.service.alerts;

import dev.melvinchia3636.airngreen.model.AlertThreshold;
import dev.melvinchia3636.airngreen.model.AqiAlert;
import dev.melvinchia3636.airngreen.model.AqiLevel;
import dev.melvinchia3636.airngreen.model.StationReading;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * Subject of the pollution-alert observer pattern.
 *
 * <p>
 * A reading is announced when it reaches the selected band and is worse than
 * the last announced band. Repeated refreshes inside the same band therefore
 * fire once, while a drop below the threshold re-arms the subject.
 */
public final class PollutionAlertService {

    private final CopyOnWriteArrayList<AlertObserver> observers = new CopyOnWriteArrayList<>();
    private final Supplier<AlertThreshold> threshold;

    private AqiLevel lastAnnounced;

    /**
     * Creates the subject.
     *
     * @param threshold supplies the threshold currently chosen in settings
     */
    public PollutionAlertService(Supplier<AlertThreshold> threshold) {
        this.threshold = threshold;
    }

    /**
     * Subscribes an observer.
     *
     * @param observer observer to add; duplicates are ignored
     */
    public void register(AlertObserver observer) {
        observers.addIfAbsent(observer);
    }

    /**
     * Removes a subscription.
     *
     * @param observer observer to remove
     */
    public void unregister(AlertObserver observer) {
        observers.remove(observer);
    }

    /**
     * Evaluates a fresh reading and notifies observers on a crossing.
     *
     * @param reading latest station reading
     */
    public void evaluate(StationReading reading) {
        AlertThreshold current = threshold.get();

        if (!current.isEnabled()) {
            lastAnnounced = null;

            return;
        }

        AqiLevel level = reading.level();
        AqiLevel required = current.level().orElseThrow();

        if (level == AqiLevel.UNKNOWN || !level.isAtLeast(required)) {
            lastAnnounced = null;

            return;
        }
        if (lastAnnounced != null && !level.isWorseThan(lastAnnounced)) {
            return;
        }

        lastAnnounced = level;
        announce(
            new AqiAlert(
                level,
                reading.aqi(),
                reading.name(),
                System.currentTimeMillis()
            )
        );
    }

    private void announce(AqiAlert alert) {
        for (AlertObserver observer : observers) {
            observer.onThresholdExceeded(alert);
        }
    }
}
