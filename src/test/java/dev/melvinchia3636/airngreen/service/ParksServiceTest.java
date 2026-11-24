package dev.melvinchia3636.airngreen.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.melvinchia3636.airngreen.model.District;
import dev.melvinchia3636.airngreen.model.NearbyPark;
import dev.melvinchia3636.airngreen.model.Park;

class ParksServiceTest {

    private static final List<Park> PARKS = List.of(
            new Park("Near", District.KUALA_LUMPUR, 3.1400, 101.6870, "trail"),
            new Park("Middle", District.PETALING_JAYA, 3.1073, 101.6067, "lake"),
            new Park("Far", District.PUTRAJAYA, 2.9264, 101.6964, "wetland"));

    private final ParksService service = new ParksService(PARKS);

    @Test
    void nearest_ordersByDistance() {
        List<NearbyPark> nearest = service.nearest(District.KUALA_LUMPUR, 3);

        assertAll(
                () -> assertEquals("Near", nearest.get(0).park().name()),
                () -> assertEquals("Far", nearest.get(2).park().name()),
                () -> assertTrue(nearest.get(0).distanceKm()
                        < nearest.get(1).distanceKm()));
    }

    @Test
    void nearest_respectsTheLimit() {
        assertEquals(2, service.nearest(District.KUALA_LUMPUR, 2).size());
    }

    @Test
    void nearest_rejectsNonPositiveLimit() {
        assertThrows(IllegalArgumentException.class,
                () -> service.nearest(District.KUALA_LUMPUR, 0));
    }

    @Test
    void distance_isZeroForTheSamePoint() {
        assertEquals(0, ParksService.distanceKm(3.139, 101.6869,
                3.139, 101.6869), 0.000001);
    }

    @Test
    void distance_isSymmetric() {
        double forward = ParksService.distanceKm(3.139, 101.6869,
                2.9264, 101.6964);
        double backward = ParksService.distanceKm(2.9264, 101.6964,
                3.139, 101.6869);

        assertAll(
                () -> assertEquals(forward, backward, 0.000001),
                () -> assertTrue(forward > 20 && forward < 25));
    }

    @Test
    void fromClasspath_loadsTheBundledDataset() throws IOException {
        ParksService bundled = ParksService.fromClasspath();

        assertAll(
                () -> assertTrue(bundled.all().size() >= 10),
                () -> assertTrue(bundled.all().stream().anyMatch(
                        park -> park.district() == District.KUALA_LUMPUR)),
                () -> assertTrue(bundled.all().stream().anyMatch(
                        park -> park.district() == District.PUTRAJAYA)));
    }

    @Test
    void bundledDataset_coversEveryDistrict() throws IOException {
        ParksService bundled = ParksService.fromClasspath();

        for (District district : District.values()) {
            assertTrue(bundled.all().stream()
                            .anyMatch(park -> park.district() == district),
                    () -> "No park for " + district);
        }
    }

    @Test
    void fromClasspathOrEmpty_loadsTheBundledDataset() {
        assertTrue(ParksService.fromClasspathOrEmpty().all().size() >= 100);
    }
}
