package dev.melvinchia3636.airngreen.service;

import dev.melvinchia3636.airngreen.model.CitizenReport;
import dev.melvinchia3636.airngreen.model.District;
import dev.melvinchia3636.airngreen.storage.LocalStore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Validates and stores citizen pollution reports. */
public final class ReportService {

    /** Maximum accepted description length. */
    public static final int MAX_NOTE_LENGTH = 500;

    private final LocalStore store;

    /**
     * Creates the service.
     *
     * @param store persistence for the report list
     */
    public ReportService(LocalStore store) {
        this.store = store;
    }

    /**
     * Validates and saves a new report.
     *
     * @param district district the report applies to
     * @param category short category label
     * @param note     free-text description
     * @return the stored report
     * @throws IllegalArgumentException when a field is missing or too long
     */
    public CitizenReport add(District district, String category, String note) {
        if (district == null) {
            throw new IllegalArgumentException("Choose a district.");
        }

        String cleanCategory = requireText(
            category,
            "Choose a report category."
        );
        String cleanNote = requireText(note, "Describe what you observed.");
        if (cleanNote.length() > MAX_NOTE_LENGTH) {
            throw new IllegalArgumentException(
                "Keep the description under " + MAX_NOTE_LENGTH + " characters."
            );
        }

        CitizenReport report = new CitizenReport(
            UUID.randomUUID().toString(),
            district,
            cleanCategory,
            cleanNote,
            Instant.now().getEpochSecond()
        );

        List<CitizenReport> reports = new ArrayList<>(store.loadReports());
        reports.add(report);
        store.saveReports(reports);

        return report;
    }

    /**
     * Every stored report, newest first.
     *
     * @return an immutable list
     */
    public List<CitizenReport> getAll() {
        return store.loadReports().stream()
            .sorted(
                Comparator.comparingLong(CitizenReport::createdAt).reversed()
            ).toList();
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }
}
