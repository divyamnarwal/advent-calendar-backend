package com.divyam.advent.dto;

import com.divyam.advent.enums.ThemePreference;

import java.util.List;

public class ProfileResponseDto {

    private Long id;
    private String name;
    private String email;
    private String avatar;
    private String avatarPublicId;
    private String bio;
    private String accentColor;
    private String bannerUrl;
    private String bannerPublicId;
    private String featuredBadgeId;
    private String featuredBadgeTitle;
    private String featuredBadgeIcon;
    private SocialLinksDto socials;
    private String country;
    private Integer streak;
    private Long failedCount;
    private Double winRate;
    private Long totalPoints;
    private long elo;
    private int level;
    private long levelMinElo;
    private Long levelMaxElo;
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
            String bio,
            String accentColor,
            String bannerUrl,
            String featuredBadgeId,
            String featuredBadgeTitle,
            String featuredBadgeIcon,
            SocialLinksDto socials,
            Integer streak,
            Long totalPoints,
            long elo,
            int level,
            long levelMinElo,
            Long levelMaxElo,
            List<String> badges,
            ThemePreference themePreference,
            List<String> newlyUnlockedBadgeIds
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatar = avatar;
        this.bio = bio;
        this.accentColor = accentColor;
        this.bannerUrl = bannerUrl;
        this.featuredBadgeId = featuredBadgeId;
        this.featuredBadgeTitle = featuredBadgeTitle;
        this.featuredBadgeIcon = featuredBadgeIcon;
        this.socials = socials;
        this.streak = streak;
        this.totalPoints = totalPoints;
        this.elo = elo;
        this.level = level;
        this.levelMinElo = levelMinElo;
        this.levelMaxElo = levelMaxElo;
        this.badges = badges;
        this.themePreference = themePreference;
        this.newlyUnlockedBadgeIds = newlyUnlockedBadgeIds;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getFeaturedBadgeId() {
        return featuredBadgeId;
    }

    public void setFeaturedBadgeId(String featuredBadgeId) {
        this.featuredBadgeId = featuredBadgeId;
    }

    public String getFeaturedBadgeTitle() {
        return featuredBadgeTitle;
    }

    public void setFeaturedBadgeTitle(String featuredBadgeTitle) {
        this.featuredBadgeTitle = featuredBadgeTitle;
    }

    public String getFeaturedBadgeIcon() {
        return featuredBadgeIcon;
    }

    public void setFeaturedBadgeIcon(String featuredBadgeIcon) {
        this.featuredBadgeIcon = featuredBadgeIcon;
    }

    public SocialLinksDto getSocials() {
        return socials;
    }

    public void setSocials(SocialLinksDto socials) {
        this.socials = socials;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Long getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Long failedCount) {
        this.failedCount = failedCount;
    }

    public Double getWinRate() {
        return winRate;
    }

    public void setWinRate(Double winRate) {
        this.winRate = winRate;
    }

    public long getElo() {
        return elo;
    }

    public void setElo(long elo) {
        this.elo = elo;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getLevelMinElo() {
        return levelMinElo;
    }

    public void setLevelMinElo(long levelMinElo) {
        this.levelMinElo = levelMinElo;
    }

    public Long getLevelMaxElo() {
        return levelMaxElo;
    }

    public void setLevelMaxElo(Long levelMaxElo) {
        this.levelMaxElo = levelMaxElo;
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

    public String getAvatarPublicId() {
        return avatarPublicId;
    }

    public void setAvatarPublicId(String avatarPublicId) {
        this.avatarPublicId = avatarPublicId;
    }

    public String getBannerPublicId() {
        return bannerPublicId;
    }

    public void setBannerPublicId(String bannerPublicId) {
        this.bannerPublicId = bannerPublicId;
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
