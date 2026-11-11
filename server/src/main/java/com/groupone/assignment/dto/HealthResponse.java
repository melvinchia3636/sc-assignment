package com.groupone.assignment.dto;

import java.time.Instant;

/**
 * Response body for {@code GET /health}.
 */
public record HealthResponse(String status, Instant timestamp) {
}
