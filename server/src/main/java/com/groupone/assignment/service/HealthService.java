package com.groupone.assignment.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.groupone.assignment.dto.HealthResponse;

@Service
public class HealthService {

	public HealthResponse getHealth() {
		return new HealthResponse("UP", Instant.now());
	}

}
