package dev.melvinchia3636.airngreen.model;

/**
 * A pollution observation submitted by a resident.
 *
 * @param id generated identifier
 * @param district district the report applies to
 * @param category short category such as "Open burning"
 * @param note free-text description
 * @param createdAt submission time, in seconds since the Unix epoch
 */
public record CitizenReport(
    String id,
    District district,
    String category,
    String note,
    long createdAt
) {
}
