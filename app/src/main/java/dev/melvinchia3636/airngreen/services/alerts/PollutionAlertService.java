package dev.melvinchia3636.airngreen.services.alerts;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import dev.melvinchia3636.airngreen.repositories.model.StationDetail;
import dev.melvinchia3636.airngreen.repositories.options.AlertThreshold;
import dev.melvinchia3636.airngreen.repositories.policy.AqiLevel;
import dev.melvinchia3636.airngreen.services.AlertThresholdService;

/**
 * Subject of the pollution-alert observer pattern. A reading is announced when
 * it reaches the chosen band and is worse than the last announced one, so a
 * refresh does not repeat an alert.
 */
public final class PollutionAlertService {

    private static volatile PollutionAlertService instance;

    public static PollutionAlertService get(Context context) {
        PollutionAlertService current = instance;
        if (current == null) {
            synchronized (PollutionAlertService.class) {
                current = instance;
                if (current == null) {
                    current = create(context.getApplicationContext());
                    instance = current;
                }
            }
        }
        return current;
    }

    private static PollutionAlertService create(Context applicationContext) {
        PollutionAlertService service = new PollutionAlertService(
                () -> AlertThresholdService.INSTANCE.current(applicationContext));
        service.register(service.feed);

        NotificationAlertObserver notifier =
                new NotificationAlertObserver(applicationContext);
        notifier.createChannel();
        service.register(notifier);
        return service;
    }

    private final CopyOnWriteArrayList<AlertObserver> observers = new CopyOnWriteArrayList<>();
    private final AlertFeed feed = new AlertFeed();
    private final Supplier<AlertThreshold> threshold;

    private AqiLevel lastAnnounced;

    public PollutionAlertService(Supplier<AlertThreshold> threshold) {
        this.threshold = threshold;
    }

    public void register(AlertObserver observer) {
        observers.addIfAbsent(observer);
    }

    public void unregister(AlertObserver observer) {
        observers.remove(observer);
    }

    /** Observable feed for in-app banners. */
    public LiveData<AqiAlert> latestAlert() {
        return feed.latest();
    }

    /** Evaluates a fresh reading and notifies every observer on a crossing. */
    public void onReading(StationDetail station) {
        AlertThreshold current = threshold.get();
        if (!current.isEnabled()) {
            lastAnnounced = null;
            return;
        }

        AqiLevel level = AqiLevel.classify(station.getAqi());
        if (level == AqiLevel.UNKNOWN || level.ordinal() < current.level().ordinal()) {
            lastAnnounced = null;
            return;
        }
        if (lastAnnounced != null && level.ordinal() <= lastAnnounced.ordinal()) {
            return;
        }

        lastAnnounced = level;
        AqiAlert alert = new AqiAlert(level, station.getAqi(), station.getName(),
                System.currentTimeMillis());
        for (AlertObserver observer : observers) {
            observer.onThresholdExceeded(alert);
        }
    }
}
