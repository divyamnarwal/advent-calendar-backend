package com.divyam.advent.dto;

import com.divyam.advent.enums.ModerationStatus;

import java.time.LocalDateTime;

public record AdminUserChallengeDto(
        Long id,
        Long userId,
        String userName,
        String userEmail,
        Long challengeId,
        String challengeTitle,
        String challengeCategory,
        LocalDateTime completionTime,
        String proofPhotoUrl,
        String userReflection,
        ModerationStatus moderationStatus
) {}
