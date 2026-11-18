package dev.melvinchia3636.airngreen.services.alerts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.melvinchia3636.airngreen.repositories.model.StationDetail;
import dev.melvinchia3636.airngreen.repositories.options.AlertThreshold;
import dev.melvinchia3636.airngreen.repositories.policy.AqiLevel;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class PollutionAlertServiceTest {

    private final List<AqiAlert> received = new ArrayList<>();
    private final AlertObserver observer = received::add;

    @Test
    void registeredObservers_receiveCrossings() {
        PollutionAlertService service = service(AlertThreshold.MODERATE);
        service.register(observer);

        service.onReading(station(90));

        assertEquals(1, received.size());
        assertEquals(AqiLevel.MODERATE, received.get(0).level());
    }

    @Test
    void unregisteredObservers_stopReceiving() {
        PollutionAlertService service = service(AlertThreshold.MODERATE);
        service.register(observer);
        service.unregister(observer);

        service.onReading(station(90));

        assertEquals(0, received.size());
    }

    @Test
    void everyObserver_isNotified() {
        PollutionAlertService service = service(AlertThreshold.MODERATE);
        List<AqiAlert> second = new ArrayList<>();
        service.register(observer);
        service.register(second::add);

        service.onReading(station(90));

        assertEquals(1, received.size());
        assertEquals(1, second.size());
    }

    @Test
    void sameBand_isAnnouncedOnce() {
        PollutionAlertService service = service(AlertThreshold.MODERATE);
        service.register(observer);

        service.onReading(station(70));
        service.onReading(station(90));

        assertEquals(1, received.size());
    }

    @Test
    void worseBand_alertsAgain() {
        PollutionAlertService service = service(AlertThreshold.MODERATE);
        service.register(observer);

        service.onReading(station(70));
        service.onReading(station(130));

        assertEquals(2, received.size());
        assertEquals(AqiLevel.SENSITIVE, received.get(1).level());
    }

    @Test
    void disabledThreshold_neverAlerts() {
        PollutionAlertService service = service(AlertThreshold.OFF);
        service.register(observer);

        service.onReading(station(300));

        assertEquals(0, received.size());
    }

    private static PollutionAlertService service(AlertThreshold threshold) {
        return new PollutionAlertService(() -> threshold);
    }

    private static StationDetail station(double aqi) {
        return new StationDetail("idx", "Station", aqi, 0L,
                Collections.emptyMap(), Collections.emptyMap());
    }
}
