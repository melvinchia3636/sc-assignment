package dev.melvinchia3636.airngreen.service.alerts;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.melvinchia3636.airngreen.model.AqiAlert;
import dev.melvinchia3636.airngreen.model.AqiLevel;
import dev.melvinchia3636.airngreen.service.LocalStore;

class AlertArchiveObserverTest {

    @TempDir
    Path directory;

    @Test
    void archivesAlertsNewestFirst() {
        LocalStore store = new LocalStore(directory);
        AlertArchiveObserver observer = new AlertArchiveObserver(store);

        observer.onThresholdExceeded(alert(90, 1));
        observer.onThresholdExceeded(alert(130, 2));

        List<AqiAlert> archived = store.loadAlerts();

        assertAll(
                () -> assertEquals(2, archived.size()),
                () -> assertEquals(130, archived.get(0).aqi()),
                () -> assertEquals(90, archived.get(1).aqi()));
    }

    @Test
    void keepsTheArchiveBounded() {
        LocalStore store = new LocalStore(directory);
        AlertArchiveObserver observer = new AlertArchiveObserver(store);

        for (int index = 0; index < 55; index++) {
            observer.onThresholdExceeded(alert(90 + index, index));
        }

        List<AqiAlert> archived = store.loadAlerts();

        assertAll(
                () -> assertEquals(50, archived.size()),
                () -> assertEquals(144, archived.get(0).aqi()));
    }

    @Test
    void archivedAlertsSurviveANewObserver() {
        LocalStore store = new LocalStore(directory);
        new AlertArchiveObserver(store).onThresholdExceeded(alert(90, 1));

        AlertLogObserver history = new AlertLogObserver(store.loadAlerts());

        assertEquals(1, history.alerts().size());
    }

    private static AqiAlert alert(double aqi, long createdAt) {
        return new AqiAlert(AqiLevel.classify(aqi), aqi, "Kapit", createdAt);
    }
}
