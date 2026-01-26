package com.divyam.advent.dto;

import com.divyam.advent.enums.Mood;

/**
 * Data Transfer Object for Global Student Pulse analytics.
 * Contains anonymized daily statistics about user engagement and mood.
 * No user identifiers are exposed.
 */
public class PulseResponseDto {

    private String date;                    // Today's date (YYYY-MM-DD)
    private long totalUsers;                // Total unique users who received a challenge today
    private long completedCount;            // Users who completed today's challenge
    private double completionPercentage;    // (completed / total) * 100

    private long lowMoodCount;              // Users feeling LOW today
    private long neutralMoodCount;          // Users feeling NEUTRAL today
    private long highMoodCount;             // Users feeling HIGH today
    private String averageMood;             // Dominant mood: LOW / NEUTRAL / HIGH

    private boolean hasData;                // Whether any challenges were assigned today

    /**
     * Constructor for today's pulse data.
     *
     * @param date today's date string
     * @param totalUsers total users assigned challenges today
     * @param completedCount users who completed today
     * @param lowMoodCount users with LOW mood
     * @param neutralMoodCount users with NEUTRAL mood
     * @param highMoodCount users with HIGH mood
     */
    public PulseResponseDto(String date, long totalUsers, long completedCount,
                            long lowMoodCount, long neutralMoodCount, long highMoodCount) {
        this.date = date;
        this.totalUsers = totalUsers;
        this.completedCount = completedCount;
        this.lowMoodCount = lowMoodCount;
        this.neutralMoodCount = neutralMoodCount;
        this.highMoodCount = highMoodCount;

        // Calculate completion percentage with division-by-zero protection
        this.completionPercentage = totalUsers > 0
            ? (completedCount * 100.0 / totalUsers)
            : 0.0;

        // Determine dominant mood
        this.averageMood = calculateDominantMood(lowMoodCount, neutralMoodCount, highMoodCount);

        // Has data if at least one user was assigned a challenge
        this.hasData = totalUsers > 0;
    }

    /**
     * Empty constructor for no-data scenario.
     */
    public PulseResponseDto() {
        this.hasData = false;
    }

    /**
     * Determines the dominant mood based on counts.
     * Ties are resolved in order: HIGH > NEUTRAL > LOW
     */
    private String calculateDominantMood(long low, long neutral, long high) {
        if (high >= neutral && high >= low) {
            return Mood.HIGH.name();
        } else if (neutral >= low) {
            return Mood.NEUTRAL.name();
        } else {
            return Mood.LOW.name();
        }
    }

    // Getters - DTO is immutable, no setters

    public String getDate() {
        return date;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    public long getLowMoodCount() {
        return lowMoodCount;
    }

    public long getNeutralMoodCount() {
        return neutralMoodCount;
    }

    public long getHighMoodCount() {
        return highMoodCount;
    }

    public String getAverageMood() {
        return averageMood;
    }

    public boolean isHasData() {
        return hasData;
    }
}
