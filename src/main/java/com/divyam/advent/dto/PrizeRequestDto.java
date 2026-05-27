package com.divyam.advent.dto;

/**
 * Create/update payload for a prize. Validated in the service:
 * title required; FASTEST_CHALLENGE requires targetChallengeId; criteria must be valid.
 */
public record PrizeRequestDto(
        String title,
        String description,
        String criteria,
        Long targetChallengeId,
        String periodMonth,
        Boolean active
) {}
