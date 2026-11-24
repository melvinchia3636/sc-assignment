package dev.melvinchia3636.airngreen.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.melvinchia3636.airngreen.model.District;
import dev.melvinchia3636.airngreen.model.NearbyPark;
import dev.melvinchia3636.airngreen.model.Park;

import org.junit.jupiter.api.Test;

import java.util.List;

class ParksServiceTest {

    private static final List<Park> PARKS = List.of(
        new Park("Near", District.KUALA_LUMPUR, 3.1400, 101.6870, "trail"),
        new Park("Middle", District.PETALING_JAYA, 3.1073, 101.6067, "lake"),
        new Park("Far", District.PUTRAJAYA, 2.9264, 101.6964, "wetland")
    );

    private final ParksService service = new ParksService(PARKS);

    @Test
    void nearest_ordersByDistance() {
        List<NearbyPark> nearest = service.nearest(District.KUALA_LUMPUR);

        assertAll(
            () -> assertEquals(3, nearest.size()),
            () -> assertEquals("Near", nearest.get(0).park().name()),
            () -> assertEquals("Far", nearest.get(2).park().name()),
            () -> assertTrue(
                nearest.get(0).distanceKm() < nearest.get(1).distanceKm()
            )
        );
    }

    @Test
    void distance_isZeroForTheSamePoint() {
        assertEquals(
            0,
            GeoDistance.kmBetween(3.139, 101.6869, 3.139, 101.6869),
            0.000001
        );
    }

    @Test
    void distance_isSymmetric() {
        double forward = GeoDistance
            .kmBetween(3.139, 101.6869, 2.9264, 101.6964);
        double backward = GeoDistance
            .kmBetween(2.9264, 101.6964, 3.139, 101.6869);

        assertAll(
            () -> assertEquals(forward, backward, 0.000001),
            () -> assertTrue(forward > 20 && forward < 25)
        );
    }

    @Test
    void fromClasspath_coversEveryDistrict() {
        ParksService bundled = ParksService.fromClasspath();

        for (District district : District.values()) {
            assertFalse(
                bundled.nearest(district).isEmpty(),
                () -> "No park data for " + district
            );
        }
    }
}
