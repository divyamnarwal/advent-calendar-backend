package com.divyam.advent.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

public class MonthlyRecapResponseDto {
    private String month;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime rangeStart;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime rangeEnd;
    private long totalAssignedThisMonth;
    private long totalCompletedThisMonth;
    private int currentStreakDays;
    private int longestStreakDays;
    private String topCategory;
    private long topCategoryCount;
    private long capsulesCreatedThisMonth;
    private long capsulesUnlockedThisMonth;
    private long photosAddedThisMonth;
    private List<RecapPhotoPreviewDto> recentPhotos;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime generatedAt;

    public MonthlyRecapResponseDto() {
    }

    public MonthlyRecapResponseDto(
            String month,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            long totalAssignedThisMonth,
            long totalCompletedThisMonth,
            int currentStreakDays,
            int longestStreakDays,
            String topCategory,
            long topCategoryCount,
            long capsulesCreatedThisMonth,
            long capsulesUnlockedThisMonth,
            long photosAddedThisMonth,
            List<RecapPhotoPreviewDto> recentPhotos,
            LocalDateTime generatedAt
    ) {
        this.month = month;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.totalAssignedThisMonth = totalAssignedThisMonth;
        this.totalCompletedThisMonth = totalCompletedThisMonth;
        this.currentStreakDays = currentStreakDays;
        this.longestStreakDays = longestStreakDays;
        this.topCategory = topCategory;
        this.topCategoryCount = topCategoryCount;
        this.capsulesCreatedThisMonth = capsulesCreatedThisMonth;
        this.capsulesUnlockedThisMonth = capsulesUnlockedThisMonth;
        this.photosAddedThisMonth = photosAddedThisMonth;
        this.recentPhotos = recentPhotos;
        this.generatedAt = generatedAt;
    }

    public String getMonth() {
        return month;
    }

    public LocalDateTime getRangeStart() {
        return rangeStart;
    }

    public LocalDateTime getRangeEnd() {
        return rangeEnd;
    }

    public long getTotalAssignedThisMonth() {
        return totalAssignedThisMonth;
    }

    public long getTotalCompletedThisMonth() {
        return totalCompletedThisMonth;
    }

    public int getCurrentStreakDays() {
        return currentStreakDays;
    }

    public int getLongestStreakDays() {
        return longestStreakDays;
    }

    public String getTopCategory() {
        return topCategory;
    }

    public long getTopCategoryCount() {
        return topCategoryCount;
    }

    public long getCapsulesCreatedThisMonth() {
        return capsulesCreatedThisMonth;
    }

    public long getCapsulesUnlockedThisMonth() {
        return capsulesUnlockedThisMonth;
    }

    public long getPhotosAddedThisMonth() {
        return photosAddedThisMonth;
    }

    public List<RecapPhotoPreviewDto> getRecentPhotos() {
        return recentPhotos;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
}
