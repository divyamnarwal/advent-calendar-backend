package com.divyam.advent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health check controller for monitoring application status.
 * Provides a simple endpoint to verify the backend is running.
 */
@RestController
public class HealthController {

    /**
     * Health check endpoint.
     * @return a confirmation message that the backend is running
     */
    @GetMapping("/health")
    public String health() {
        return "Backend running";
    }
}
