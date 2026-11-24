package dev.melvinchia3636.airngreen.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import dev.melvinchia3636.airngreen.model.AqiAlert;
import dev.melvinchia3636.airngreen.model.CitizenReport;
import dev.melvinchia3636.airngreen.model.StationReading;
import dev.melvinchia3636.airngreen.model.StationSummary;

/**
 * Small JSON and properties store under {@code ~/.airngreen}.
 *
 * <p>It keeps the last reading for offline use, citizen reports, the cached
 * station directory and user settings. A corrupt or missing file is treated as
 * empty data rather than an error.</p>
 */
public final class LocalStore {

    private static final String SETTINGS_FILE = "settings.properties";
    private static final String READING_FILE = "last-reading.json";
    private static final String REPORTS_FILE = "reports.json";
    private static final String STATIONS_FILE = "stations.json";
    private static final String ALERTS_FILE = "alerts.json";

    private static final Type REPORT_LIST =
            new TypeToken<List<CitizenReport>>() { }.getType();
    private static final Type STATION_LIST =
            new TypeToken<List<StationSummary>>() { }.getType();
    private static final Type ALERT_LIST =
            new TypeToken<List<AqiAlert>>() { }.getType();

    private final Path directory;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Creates a store in the current user's home directory.
     */
    public LocalStore() {
        this(Path.of(System.getProperty("user.home"), ".airngreen"));
    }

    /**
     * Creates a store rooted at an arbitrary directory, used by tests.
     *
     * @param directory folder for all data files
     */
    public LocalStore(Path directory) {
        this.directory = directory;
    }

    /**
     * Loads the last successful reading.
     *
     * @return the reading, or empty when none was saved
     */
    public Optional<StationReading> loadReading() {
        return Optional.ofNullable(read(READING_FILE, StationReading.class));
    }

    /**
     * Saves the last successful reading.
     *
     * @param reading reading to persist
     */
    public void saveReading(StationReading reading) {
        write(READING_FILE, reading);
    }

    /**
     * Loads saved citizen reports.
     *
     * @return reports in stored order, or an empty list
     */
    public List<CitizenReport> loadReports() {
        List<CitizenReport> reports = read(REPORTS_FILE, REPORT_LIST);
        return reports == null ? List.of() : List.copyOf(reports);
    }

    /**
     * Saves citizen reports.
     *
     * @param reports reports to persist
     */
    public void saveReports(List<CitizenReport> reports) {
        write(REPORTS_FILE, reports);
    }

    /**
     * Loads the cached station directory.
     *
     * @return stations in stored order, or an empty list
     */
    public List<StationSummary> loadStations() {
        List<StationSummary> stations = read(STATIONS_FILE, STATION_LIST);
        return stations == null ? List.of() : List.copyOf(stations);
    }

    /**
     * Saves the station directory.
     *
     * @param stations stations to persist
     */
    public void saveStations(List<StationSummary> stations) {
        write(STATIONS_FILE, stations);
    }

    /**
     * Loads archived pollution alerts.
     *
     * @return alerts in stored order, or an empty list
     */
    public List<AqiAlert> loadAlerts() {
        List<AqiAlert> alerts = read(ALERTS_FILE, ALERT_LIST);
        return alerts == null ? List.of() : List.copyOf(alerts);
    }

    /**
     * Saves archived pollution alerts.
     *
     * @param alerts alerts to persist
     */
    public void saveAlerts(List<AqiAlert> alerts) {
        write(ALERTS_FILE, alerts);
    }

    /**
     * Loads user settings.
     *
     * @return stored properties, or defaults when the file is missing
     */
    public Properties loadSettings() {
        Path path = path(SETTINGS_FILE);
        if (!Files.exists(path)) {
            return new Properties();
        }

        Properties settings = new Properties();
        try (Reader reader = Files.newBufferedReader(path)) {
            settings.load(reader);
        } catch (IOException unreadable) {
            return new Properties();
        }
        return settings;
    }

    /**
     * Saves user settings.
     *
     * @param settings properties to persist
     */
    public void saveSettings(Properties settings) {
        try {
            Files.createDirectories(directory);
            try (Writer writer = Files.newBufferedWriter(path(SETTINGS_FILE))) {
                settings.store(writer, "Air & Green settings");
            }
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    private <T> T read(String file, Type type) {
        Path path = path(file);
        if (!Files.exists(path)) {
            return null;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            return gson.fromJson(reader, type);
        } catch (IOException | JsonParseException unreadable) {
            return null;
        }
    }

    private void write(String file, Object value) {
        try {
            Files.createDirectories(directory);
            try (Writer writer = Files.newBufferedWriter(path(file))) {
                gson.toJson(value, writer);
            }
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    private Path path(String file) {
        return directory.resolve(file);
    }
}
