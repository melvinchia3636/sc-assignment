package com.groupone.assignment.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles the service root. Responds with 204 No Content so clients can
 * cheaply check that the server is reachable without downloading a body.
 */
@RestController
public class RootController {

	@GetMapping("/")
	public ResponseEntity<Void> root() {
		return ResponseEntity.noContent().build();
	}

}
