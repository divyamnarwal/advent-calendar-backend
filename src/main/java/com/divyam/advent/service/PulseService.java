package com.divyam.advent.service;

import com.divyam.advent.dto.PulseResponseDto;

/**
 * Service interface for Global Student Pulse analytics.
 * Provides anonymized daily statistics about user engagement and mood.
 */
public interface PulseService {

    /**
     * Get today's pulse statistics.
     * Returns anonymized aggregated data about completion rates and mood distribution.
     *
     * @return PulseResponseDto containing today's analytics
     */
    PulseResponseDto getTodayPulse();
}
