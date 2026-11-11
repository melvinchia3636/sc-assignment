package com.groupone.assignment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.groupone.assignment.dto.HealthResponse;

class HealthServiceTests {

	private final HealthService healthService = new HealthService();

	@Test
	void returnsUpStatusWithTimestamp() {
		HealthResponse response = healthService.getHealth();

		assertEquals("UP", response.status());
		assertNotNull(response.timestamp());
	}

}
