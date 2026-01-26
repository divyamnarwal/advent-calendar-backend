package com.divyam.advent.service;

import com.divyam.advent.dto.TimeCapsuleRequestDto;
import com.divyam.advent.dto.TimeCapsuleResponseDto;

import java.util.List;

/**
 * Service interface for TimeCapsule operations.
 */
public interface TimeCapsuleService {

    /**
     * Create a new time capsule for a user.
     *
     * @param userId the ID of the user creating the capsule
     * @param request the capsule request containing content and reveal info
     * @return the created capsule response
     */
    TimeCapsuleResponseDto createCapsule(Long userId, TimeCapsuleRequestDto request);

    /**
     * Get all revealed (ready to open) capsules for a user.
     * Only returns capsules whose reveal date has passed.
     *
     * @param userId the ID of the user
     * @return list of revealed capsules for this user
     */
    List<TimeCapsuleResponseDto> getRevealedCapsules(Long userId);

    /**
     * Get all pending (not yet revealed) capsules for a user.
     *
     * @param userId the ID of the user
     * @return list of pending capsules for this user
     */
    List<TimeCapsuleResponseDto> getPendingCapsules(Long userId);
}
