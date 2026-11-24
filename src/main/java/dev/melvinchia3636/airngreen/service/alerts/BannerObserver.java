package dev.melvinchia3636.airngreen.service.alerts;

import java.util.function.Consumer;

import dev.melvinchia3636.airngreen.model.AqiAlert;

/**
 * Observer that forwards an alert to the window's status banner.
 */
public final class BannerObserver implements AlertObserver {

    private final Consumer<AqiAlert> banner;

    /**
     * Creates the observer.
     *
     * @param banner callback that displays the alert
     */
    public BannerObserver(Consumer<AqiAlert> banner) {
        this.banner = banner;
    }

    @Override
    public void onThresholdExceeded(AqiAlert alert) {
        banner.accept(alert);
    }
}
