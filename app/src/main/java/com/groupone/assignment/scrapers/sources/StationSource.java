package com.groupone.assignment.scrapers.sources;

import com.groupone.assignment.repositories.ApiException;
import com.groupone.assignment.repositories.model.StationDetail;

/**
 * A remote provider of station details.
 */
public interface StationSource {

    /**
     * Fetches one station report.
     *
     * @param idx Provider-specific WAQI station id.
     * @return The report, or {@code null} when the provider has no data for the id.
     */
    StationDetail fetch(String idx) throws ApiException;
}
