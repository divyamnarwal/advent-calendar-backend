package com.divyam.advent.dto;

/**
 * Data Transfer Object for user progress statistics.
 * Used to return progress information without exposing internal entities.
 */
public class UserProgressDto {

    private Long userId;
    private String userName;
    private long totalAssigned;      // Total challenges (ASSIGNED + COMPLETED)
    private long totalCompleted;     // Only COMPLETED challenges
    private double completionPercentage;  // (completed / total) * 100

    /**
     * Constructor to create a new UserProgressDto.
     * Automatically calculates completion percentage with division-by-zero protection.
     *
     * @param userId the ID of the user
     * @param userName the name of the user
     * @param totalAssigned total number of challenges assigned to the user
     * @param totalCompleted number of challenges completed by the user
     */
    public UserProgressDto(Long userId, String userName,
                           long totalAssigned, long totalCompleted) {
        this.userId = userId;
        this.userName = userName;
        this.totalAssigned = totalAssigned;
        this.totalCompleted = totalCompleted;
        // Avoid division by zero - if no challenges assigned, percentage is 0
        this.completionPercentage = totalAssigned > 0
            ? (totalCompleted * 100.0 / totalAssigned)
            : 0.0;
    }

    // Getters - DTO is immutable, no setters

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public long getTotalAssigned() {
        return totalAssigned;
    }

    public long getTotalCompleted() {
        return totalCompleted;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }
}
