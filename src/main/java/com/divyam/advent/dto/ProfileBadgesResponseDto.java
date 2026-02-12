package com.divyam.advent.dto;

import java.util.List;

public class ProfileBadgesResponseDto {

    private List<ProfileBadgeDto> badges;
    private List<ProfileBadgeDto> earnedBadges;
    private List<String> newlyUnlockedBadgeIds;

    public ProfileBadgesResponseDto() {
    }

    public ProfileBadgesResponseDto(
            List<ProfileBadgeDto> badges,
            List<ProfileBadgeDto> earnedBadges,
            List<String> newlyUnlockedBadgeIds
    ) {
        this.badges = badges;
        this.earnedBadges = earnedBadges;
        this.newlyUnlockedBadgeIds = newlyUnlockedBadgeIds;
    }

    public List<ProfileBadgeDto> getBadges() {
        return badges;
    }

    public void setBadges(List<ProfileBadgeDto> badges) {
        this.badges = badges;
    }

    public List<ProfileBadgeDto> getEarnedBadges() {
        return earnedBadges;
    }

    public void setEarnedBadges(List<ProfileBadgeDto> earnedBadges) {
        this.earnedBadges = earnedBadges;
    }

    public List<String> getNewlyUnlockedBadgeIds() {
        return newlyUnlockedBadgeIds;
    }

    public void setNewlyUnlockedBadgeIds(List<String> newlyUnlockedBadgeIds) {
        this.newlyUnlockedBadgeIds = newlyUnlockedBadgeIds;
    }
}
