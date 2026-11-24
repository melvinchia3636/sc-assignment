package dev.melvinchia3636.airngreen.service.alerts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import dev.melvinchia3636.airngreen.model.AqiAlert;
import dev.melvinchia3636.airngreen.model.AqiLevel;
import dev.melvinchia3636.airngreen.model.AlertThreshold;
import dev.melvinchia3636.airngreen.model.StationReading;

class PollutionAlertServiceTest {

    private final List<AqiAlert> received = new ArrayList<>();
    private final AlertObserver observer = received::add;
    private final AtomicReference<AlertThreshold> threshold =
            new AtomicReference<>(AlertThreshold.MODERATE);
    private final PollutionAlertService service =
            new PollutionAlertService(threshold::get);

    @Test
    void registeredObservers_receiveCrossings() {
        service.register(observer);

        service.evaluate(station(90));

        assertEquals(1, received.size());
        assertEquals(AqiLevel.MODERATE, received.get(0).level());
    }

    @Test
    void unregisteredObservers_stopReceiving() {
        service.register(observer);
        service.unregister(observer);

        service.evaluate(station(90));

        assertTrue(received.isEmpty());
    }

    @Test
    void everyObserver_isNotified() {
        List<AqiAlert> second = new ArrayList<>();
        service.register(observer);
        service.register(second::add);

        service.evaluate(station(90));

        assertEquals(1, received.size());
        assertEquals(1, second.size());
    }

    @Test
    void sameBand_isAnnouncedOnce() {
        service.register(observer);

        service.evaluate(station(70));
        service.evaluate(station(90));

        assertEquals(1, received.size());
    }

    @Test
    void worseBand_alertsAgain() {
        service.register(observer);

        service.evaluate(station(70));
        service.evaluate(station(130));

        assertEquals(2, received.size());
        assertEquals(AqiLevel.SENSITIVE, received.get(1).level());
    }

    @Test
    void disabledThreshold_neverAlerts() {
        threshold.set(AlertThreshold.OFF);
        service.register(observer);

        service.evaluate(station(300));

        assertTrue(received.isEmpty());
    }

    @Test
    void droppingBelowThreshold_rearmsTheAlert() {
        service.register(observer);

        service.evaluate(station(130));
        service.evaluate(station(40));
        service.evaluate(station(90));

        assertEquals(2, received.size());
        assertEquals(AqiLevel.MODERATE, received.get(1).level());
    }

    @Test
    void unknownReading_isIgnored() {
        service.register(observer);

        service.evaluate(station(Double.NaN));

        assertTrue(received.isEmpty());
    }

    private static StationReading station(double aqi) {
        return new StationReading("2609", "Kapit", aqi, 0,
                Map.of("pm25", aqi), List.of());
    }
}
