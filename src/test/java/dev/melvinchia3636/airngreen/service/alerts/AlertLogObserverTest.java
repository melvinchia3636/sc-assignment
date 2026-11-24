package dev.melvinchia3636.airngreen.service.alerts;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.melvinchia3636.airngreen.model.AqiAlert;
import dev.melvinchia3636.airngreen.model.AqiLevel;

class AlertLogObserverTest {

    private final AlertLogObserver observer = new AlertLogObserver();

    @Test
    void recordsAlertsNewestFirst() {
        observer.onThresholdExceeded(alert(90, 1));
        observer.onThresholdExceeded(alert(130, 2));

        List<AqiAlert> alerts = observer.alerts();

        assertAll(
                () -> assertEquals(2, alerts.size()),
                () -> assertEquals(130, alerts.get(0).aqi()),
                () -> assertEquals(90, alerts.get(1).aqi()));
    }

    @Test
    void onAlertRecorded_receivesEveryAlert() {
        List<AqiAlert> received = new ArrayList<>();
        observer.onAlertRecorded(received::add);

        observer.onThresholdExceeded(alert(90, 1));
        observer.onThresholdExceeded(alert(130, 2));

        assertEquals(2, received.size());
        assertEquals(130, received.get(1).aqi());
    }

    @Test
    void onAlertRecorded_supportsMultipleListeners() {
        List<AqiAlert> first = new ArrayList<>();
        List<AqiAlert> second = new ArrayList<>();
        observer.onAlertRecorded(first::add);
        observer.onAlertRecorded(second::add);

        observer.onThresholdExceeded(alert(90, 1));

        assertAll(
                () -> assertEquals(1, first.size()),
                () -> assertEquals(1, second.size()));
    }

    @Test
    void alerts_isAnImmutableSnapshot() {
        observer.onThresholdExceeded(alert(90, 1));

        List<AqiAlert> snapshot = observer.alerts();

        assertThrows(UnsupportedOperationException.class, snapshot::clear);
    }

    @Test
    void alerts_isEmptyBeforeAnyAlert() {
        assertTrue(observer.alerts().isEmpty());
    }

    private static AqiAlert alert(double aqi, long createdAt) {
        return new AqiAlert(AqiLevel.classify(aqi), aqi, "Kapit", createdAt);
    }
}
