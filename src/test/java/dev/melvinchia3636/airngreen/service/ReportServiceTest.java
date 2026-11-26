package dev.melvinchia3636.airngreen.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.melvinchia3636.airngreen.model.CitizenReport;
import dev.melvinchia3636.airngreen.model.District;
import dev.melvinchia3636.airngreen.storage.LocalStore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

class ReportServiceTest {

    @TempDir
    Path directory;

    private LocalStore store;
    private ReportService service;

    @BeforeEach
    void createService() {
        store = new LocalStore(directory);
        service = new ReportService(store);
    }

    @Test
    void add_storesAndReturnsTheReport() {
        CitizenReport report = service.add(
            District.KUALA_LUMPUR,
            "Open burning",
            "Smoke behind the market."
        );

        assertAll(
            () -> assertEquals(District.KUALA_LUMPUR, report.district()),
            () -> assertEquals("Open burning", report.category()),
            () -> assertEquals("Smoke behind the market.", report.note()),
            () -> assertEquals(1, service.getAll().size())
        );
    }

    @Test
    void add_trimsWhitespace() {
        CitizenReport report = service
            .add(District.KLANG, "  Other  ", "  Dust from the site.  ");

        assertAll(
            () -> assertEquals("Other", report.category()),
            () -> assertEquals("Dust from the site.", report.note())
        );
    }

    @Test
    void add_rejectsBlankNote() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.add(District.KUALA_LUMPUR, "Other", "   ")
        );
    }

    @Test
    void add_rejectsBlankCategory() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.add(District.KUALA_LUMPUR, "", "Something smells.")
        );
    }

    @Test
    void add_rejectsOverlongNote() {
        String tooLong = "x".repeat(ReportService.MAX_NOTE_LENGTH + 1);

        assertThrows(
            IllegalArgumentException.class,
            () -> service.add(District.KUALA_LUMPUR, "Other", tooLong)
        );
    }

    @Test
    void add_rejectsMissingDistrict() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.add(null, "Other", "Smoke.")
        );
    }

    @Test
    void add_rejectsNullCategoryAndNote() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.add(District.KUALA_LUMPUR, null, "Smoke.")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> service.add(District.KUALA_LUMPUR, "Other", null)
        );
    }

    @Test
    void all_returnsNewestFirst() {
        store.saveReports(
            List.of(
                new CitizenReport(
                    "old",
                    District.KUALA_LUMPUR,
                    "Other",
                    "old",
                    1
                ),
                new CitizenReport(
                    "new",
                    District.KUALA_LUMPUR,
                    "Other",
                    "new",
                    2
                )
            )
        );

        assertEquals("new", service.getAll().get(0).id());
    }

    @Test
    void reports_persistAcrossServiceInstances() {
        service.add(District.SHAH_ALAM, "Construction dust", "Dust cloud.");

        ReportService reopened = new ReportService(new LocalStore(directory));

        assertEquals(1, reopened.getAll().size());
    }
}
