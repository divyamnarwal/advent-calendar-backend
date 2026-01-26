package com.divyam.advent.controller;

import com.divyam.advent.dto.PulseResponseDto;
import com.divyam.advent.service.PulseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Global Student Pulse analytics.
 * Provides anonymized daily statistics about user engagement.
 */
@RestController
@RequestMapping("/pulse")
public class PulseController {

    private final PulseService pulseService;

    @Autowired
    public PulseController(PulseService pulseService) {
        this.pulseService = pulseService;
    }

    /**
     * Get today's pulse statistics.
     * Returns anonymized aggregated data about completion rates and mood distribution.
     *
     * Example response:
     * GET /pulse/today
     *
     * {
     *   "date": "2026-01-18",
     *   "totalUsers": 150,
     *   "completedCount": 87,
     *   "completionPercentage": 58.0,
     *   "lowMoodCount": 30,
     *   "neutralMoodCount": 75,
     *   "highMoodCount": 45,
     *   "averageMood": "NEUTRAL",
     *   "hasData": true
     * }
     *
     * @return today's pulse statistics
     */
    @GetMapping("/today")
    public ResponseEntity<PulseResponseDto> getTodayPulse() {
        PulseResponseDto pulse = pulseService.getTodayPulse();
        return ResponseEntity.ok(pulse);
    }
}
