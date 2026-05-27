package com.divyam.advent.dto;

import com.divyam.advent.enums.Culture;

import java.time.LocalDateTime;

public record AdminUserSummaryDto(
        Long id,
        String name,
        String email,
        Culture country,
        String avatar,
        String bannerUrl,
        long completedCount,
        long assignedCount,
        boolean isAdmin,
        boolean isSuperAdmin,
        boolean banned,
        String banReason,
        LocalDateTime banExpiresAt
) {}
