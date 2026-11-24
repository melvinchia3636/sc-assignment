package dev.melvinchia3636.airngreen.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.melvinchia3636.airngreen.model.AqiAlert;
import dev.melvinchia3636.airngreen.model.AqiLevel;
import dev.melvinchia3636.airngreen.model.CitizenReport;
import dev.melvinchia3636.airngreen.model.District;
import dev.melvinchia3636.airngreen.model.SeriesPoint;
import dev.melvinchia3636.airngreen.model.StationReading;
import dev.melvinchia3636.airngreen.model.StationSummary;

class LocalStoreTest {

    @TempDir
    Path directory;

    @Test
    void reading_roundTripsThroughDisk() {
        LocalStore store = new LocalStore(directory);
        store.saveReading(reading());

        StationReading loaded = store.loadReading().orElseThrow();

        assertAll(
                () -> assertEquals("2609", loaded.idx()),
                () -> assertEquals("Kapit", loaded.name()),
                () -> assertEquals(25, loaded.aqi()),
                () -> assertEquals(13, loaded.pollutant("pm25").orElseThrow()),
                () -> assertEquals(2, loaded.series().size()),
                () -> assertEquals(25, loaded.series().get(1).value()));
    }

    @Test
    void reports_roundTripThroughDisk() {
        LocalStore store = new LocalStore(directory);
        CitizenReport report = new CitizenReport("id", District.KLANG,
                "Other", "Smoke", 10);
        store.saveReports(List.of(report));

        List<CitizenReport> loaded = store.loadReports();

        assertAll(
                () -> assertEquals(1, loaded.size()),
                () -> assertEquals("id", loaded.get(0).id()),
                () -> assertEquals(District.KLANG, loaded.get(0).district()));
    }

    @Test
    void stations_roundTripThroughDisk() {
        LocalStore store = new LocalStore(directory);
        store.saveStations(List.of(
                new StationSummary("1", "One", 3.0, 101.0, 42.0)));

        List<StationSummary> loaded = store.loadStations();

        assertAll(
                () -> assertEquals(1, loaded.size()),
                () -> assertEquals("One", loaded.get(0).name()),
                () -> assertEquals(42.0, loaded.get(0).aqi()));
    }

    @Test
    void alerts_roundTripThroughDisk() {
        LocalStore store = new LocalStore(directory);
        store.saveAlerts(List.of(
                new AqiAlert(AqiLevel.MODERATE, 90, "Kapit", 10)));

        List<AqiAlert> loaded = store.loadAlerts();

        assertAll(
                () -> assertEquals(1, loaded.size()),
                () -> assertEquals(AqiLevel.MODERATE, loaded.get(0).level()),
                () -> assertEquals(90, loaded.get(0).aqi()),
                () -> assertEquals("Kapit", loaded.get(0).stationName()));
    }

    @Test
    void settings_roundTripThroughDisk() {
        LocalStore store = new LocalStore(directory);
        Properties settings = new Properties();
        settings.setProperty("district", "KLANG");
        store.saveSettings(settings);

        Properties loaded = store.loadSettings();

        assertEquals("KLANG", loaded.getProperty("district"));
    }

    @Test
    void missingFiles_areTreatedAsEmpty() {
        LocalStore store = new LocalStore(directory);

        assertAll(
                () -> assertTrue(store.loadReading().isEmpty()),
                () -> assertTrue(store.loadReports().isEmpty()),
                () -> assertTrue(store.loadStations().isEmpty()),
                () -> assertTrue(store.loadSettings().isEmpty()));
    }

    @Test
    void corruptReading_isIgnored() throws IOException {
        Files.writeString(directory.resolve("last-reading.json"), "{not json");

        assertTrue(new LocalStore(directory).loadReading().isEmpty());
    }

    private StationReading reading() {
        return new StationReading("2609", "Kapit", 25, 1_795_471_200L,
                Map.of("pm25", 13.0, "pm10", 13.0),
                List.of(new SeriesPoint(1, 24), new SeriesPoint(2, 25)));
    }
}
