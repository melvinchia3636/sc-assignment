package com.groupone.assignment.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.groupone.assignment.dto.HealthResponse;
import com.groupone.assignment.service.HealthService;

/**
 * Exposes the service health endpoint.
 */
@RestController
public class HealthController {

	private final HealthService healthService;

	public HealthController(HealthService healthService) {
		this.healthService = healthService;
	}

	@GetMapping("/health")
	public HealthResponse health() {
		return healthService.getHealth();
	}

}
