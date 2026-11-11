package com.groupone.assignment.controllers;

import java.time.Instant;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reports the service health as {@code {"status":"UP","timestamp":"..."}}.
 */
@RestController
public class HealthController {

	@GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
	public HealthResponse health() {
		return new HealthResponse("UP", Instant.now());
	}

	public record HealthResponse(String status, Instant timestamp) {
	}

}
