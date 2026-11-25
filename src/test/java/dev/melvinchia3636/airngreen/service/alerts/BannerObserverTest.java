package dev.melvinchia3636.airngreen.service.alerts;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import dev.melvinchia3636.airngreen.model.AqiAlert;
import dev.melvinchia3636.airngreen.model.AqiLevel;

class BannerObserverTest {

    @Test
    void onThresholdExceeded_forwardsTheAlertToTheBanner() {
        AtomicReference<AqiAlert> shown = new AtomicReference<>();
        BannerObserver observer = new BannerObserver(shown::set);
        AqiAlert alert = new AqiAlert(AqiLevel.MODERATE, 90, "Kapit", 10);

        observer.onThresholdExceeded(alert);

        assertSame(alert, shown.get());
    }
}
