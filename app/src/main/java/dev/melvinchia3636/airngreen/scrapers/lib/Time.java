package dev.melvinchia3636.airngreen.scrapers.lib;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

/**
 * Timestamp helpers shared by the scrapers.
 */
public final class Time {

    private Time() {
    }

    /** Current wall-clock time as epoch milliseconds. */
    public static long nowMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Converts a station timestamp to epoch seconds.
     *
     * Station feeds omit the timezone for local timestamps, so the station's
     * offset is appended before parsing. Missing or null offsets default to
     * {@code +00:00}.
     */
    public static long epochSeconds(String text, String timezone) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        String value = text.trim().replace(' ', 'T');
        String timestamp = value.endsWith("Z") || value.length() > 19
                ? value
                : value + (timezone == null ? "+00:00" : timezone);
        try {
            return OffsetDateTime.parse(timestamp).toEpochSecond();
        } catch (DateTimeParseException invalidDate) {
            try {
                return Instant.parse(timestamp).getEpochSecond();
            } catch (DateTimeParseException alsoInvalid) {
                return 0;
            }
        }
    }

    public static long epochSeconds(String text) {
        return epochSeconds(text, null);
    }
}
