package com.divyam.advent.dto;

public record LeaderboardEntryDto(
        int rank,
        Long userId,
        String name,
        String avatar,
        String country,
        long elo,
        int level,
        boolean isAdmin,
        boolean isSuperAdmin
) {}
