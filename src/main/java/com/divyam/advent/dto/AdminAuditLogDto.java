package com.divyam.advent.dto;

import java.time.LocalDateTime;

public record AdminAuditLogDto(
        Long id,
        Long actorUserId,
        String actorName,
        String action,
        String targetType,
        Long targetId,
        String reason,
        LocalDateTime createdAt
) {}
