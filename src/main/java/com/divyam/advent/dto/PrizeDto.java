package com.divyam.advent.dto;

import java.time.LocalDateTime;

public record PrizeDto(
        Long id,
        String title,
        String description,
        String criteria,
        Long targetChallengeId,
        String targetChallengeTitle,
        String periodMonth,
        boolean active,
        Long awardedUserId,
        String awardedUserName,
        LocalDateTime awardedAt,
        LocalDateTime createdAt
) {}
