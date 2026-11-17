package com.groupone.assignment.guidance;

/**
 * Audience-specific air-quality guidance.
 */
public interface AudienceStrategy {

    int labelRes();

    /** Guidance note for the given AQI, or 0 when no extra note applies. */
    int noteRes(double aqi);

    /** Suggested outdoor minutes, or 0 to avoid outdoor activity. */
    int outdoorMinutes(double aqi);
}
