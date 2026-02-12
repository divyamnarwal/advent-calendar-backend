package com.divyam.advent.dto;

import com.divyam.advent.enums.ThemePreference;

import java.util.List;

public class ProfileResponseDto {

    private Long id;
    private String name;
    private String email;
    private String avatar;
    private Integer streak;
    private Long totalPoints;
    private List<String> badges;
    private ThemePreference themePreference;
    private List<String> newlyUnlockedBadgeIds;

    public ProfileResponseDto() {
    }

    public ProfileResponseDto(
            Long id,
            String name,
            String email,
            String avatar,
            Integer streak,
            Long totalPoints,
            List<String> badges,
            ThemePreference themePreference,
            List<String> newlyUnlockedBadgeIds
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatar = avatar;
        this.streak = streak;
        this.totalPoints = totalPoints;
        this.badges = badges;
        this.themePreference = themePreference;
        this.newlyUnlockedBadgeIds = newlyUnlockedBadgeIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getStreak() {
        return streak;
    }

    public void setStreak(Integer streak) {
        this.streak = streak;
    }

    public Long getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Long totalPoints) {
        this.totalPoints = totalPoints;
    }

    public List<String> getBadges() {
        return badges;
    }

    public void setBadges(List<String> badges) {
        this.badges = badges;
    }

    public ThemePreference getThemePreference() {
        return themePreference;
    }

    public void setThemePreference(ThemePreference themePreference) {
        this.themePreference = themePreference;
    }

    public List<String> getNewlyUnlockedBadgeIds() {
        return newlyUnlockedBadgeIds;
    }

    public void setNewlyUnlockedBadgeIds(List<String> newlyUnlockedBadgeIds) {
        this.newlyUnlockedBadgeIds = newlyUnlockedBadgeIds;
    }
}
