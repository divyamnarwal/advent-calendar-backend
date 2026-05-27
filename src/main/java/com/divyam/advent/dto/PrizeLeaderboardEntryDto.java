package com.divyam.advent.dto;

/**
 * One row of a prize leaderboard. {@code metric} meaning depends on the prize criterion:
 * LONGEST_STREAK = days, MOST_COMPLETED = count, FASTEST_CHALLENGE = duration in seconds.
 */
public record PrizeLeaderboardEntryDto(
        int rank,
        Long userId,
        String userName,
        String userEmail,
        long metric
) {}
