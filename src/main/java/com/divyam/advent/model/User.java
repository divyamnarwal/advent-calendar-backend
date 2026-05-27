package com.divyam.advent.model;

import com.divyam.advent.enums.Culture;
import com.divyam.advent.enums.ThemePreference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_users_auth_provider_subject",
            columnNames = {"auth_provider", "auth_subject"}
        )
    }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "country")
    private Culture country;

    @Column(name = "auth_provider")
    private String authProvider;

    @Column(name = "auth_subject")
    private String authSubject;

    @Column(name = "avatar")
    private String avatar;

    /** Cloudinary public_id paired with {@link #avatar}; used by admin cleanup to destroy the asset. */
    @Column(name = "avatar_public_id")
    private String avatarPublicId;

    // Profile customization
    @Column(name = "bio", length = 300)
    private String bio;

    /** Accent palette key (validated against an allowed set), e.g. "violet". */
    @Column(name = "accent_color", length = 32)
    private String accentColor;

    /** Id of the badge the user pins to their profile; must be one they earned. */
    @Column(name = "featured_badge_id")
    private String featuredBadgeId;

    @Column(name = "banner_url", length = 1024)
    private String bannerUrl;

    /** Cloudinary public_id paired with {@link #bannerUrl}; used by admin cleanup. */
    @Column(name = "banner_public_id")
    private String bannerPublicId;

    /**
     * Optimistic locking guard. Two concurrent {@code evaluateAndAssignBadges}
     * runs (e.g. two browser tabs completing different quests at once) would
     * otherwise silently overwrite each other's stat updates; with @Version
     * Hibernate throws {@code OptimisticLockException} on the loser, which
     * Spring surfaces as 409 — the caller can retry.
     */
    @jakarta.persistence.Version
    @Column(name = "version")
    private Long version;

    // Social links (raw handle or URL as entered; normalized to a full URL on the client).
    @Column(name = "social_vk", length = 512)
    private String socialVk;

    @Column(name = "social_telegram", length = 512)
    private String socialTelegram;

    @Column(name = "social_whatsapp", length = 512)
    private String socialWhatsapp;

    @Column(name = "social_instagram", length = 512)
    private String socialInstagram;

    @Column(name = "social_twitter", length = 512)
    private String socialTwitter;

    @Column(name = "streak", nullable = false)
    private Integer streak = 0;

    @Column(name = "total_points", nullable = false)
    private Long totalPoints = 0L;

    /** Faceit-style rating earned from completed quests; monotonic (only grows). */
    @Column(name = "elo")
    private Long elo = 0L;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_badges",
        joinColumns = @JoinColumn(name = "user_id"),
        uniqueConstraints = @UniqueConstraint(
            name = "uk_user_badges_user_badge",
            columnNames = {"user_id", "badge_id"}
        )
    )
    @Column(name = "badge_id", nullable = false)
    private Set<String> badges = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "theme_preference", nullable = false)
    private ThemePreference themePreference = ThemePreference.SYSTEM;

    // Nullable column (added via data.sql migration); the getter treats null as false
    // so pre-existing rows are read safely.
    @Column(name = "is_admin")
    private Boolean isAdmin = false;

    /** True while the user is banned. Cleared automatically once {@link #banExpiresAt} passes. */
    @Column(name = "banned")
    private Boolean banned = false;

    /** Admin-supplied reason shown on the ban screen. */
    @Column(name = "ban_reason", length = 500)
    private String banReason;

    /** When the ban auto-lifts; null = permanent until manually revoked. */
    @Column(name = "ban_expires_at")
    private LocalDateTime banExpiresAt;

    public User() {
    }

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.country = Culture.GLOBAL;
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

    public Culture getCountry() {
        return country;
    }

    public void setCountry(Culture country) {
        this.country = country;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    public String getAuthSubject() {
        return authSubject;
    }

    public void setAuthSubject(String authSubject) {
        this.authSubject = authSubject;
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

    public String getFeaturedBadgeId() {
        return featuredBadgeId;
    }

    public void setFeaturedBadgeId(String featuredBadgeId) {
        this.featuredBadgeId = featuredBadgeId;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getBannerPublicId() {
        return bannerPublicId;
    }

    public void setBannerPublicId(String bannerPublicId) {
        this.bannerPublicId = bannerPublicId;
    }

    public String getSocialVk() {
        return socialVk;
    }

    public void setSocialVk(String socialVk) {
        this.socialVk = socialVk;
    }

    public String getSocialTelegram() {
        return socialTelegram;
    }

    public void setSocialTelegram(String socialTelegram) {
        this.socialTelegram = socialTelegram;
    }

    public String getSocialWhatsapp() {
        return socialWhatsapp;
    }

    public void setSocialWhatsapp(String socialWhatsapp) {
        this.socialWhatsapp = socialWhatsapp;
    }

    public String getSocialInstagram() {
        return socialInstagram;
    }

    public void setSocialInstagram(String socialInstagram) {
        this.socialInstagram = socialInstagram;
    }

    public String getSocialTwitter() {
        return socialTwitter;
    }

    public void setSocialTwitter(String socialTwitter) {
        this.socialTwitter = socialTwitter;
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

    public long getElo() {
        return elo != null ? elo : 0L;
    }

    public void setElo(long elo) {
        this.elo = elo;
    }

    public Set<String> getBadges() {
        return badges;
    }

    public void setBadges(Set<String> badges) {
        this.badges = badges;
    }

    public ThemePreference getThemePreference() {
        return themePreference;
    }

    public void setThemePreference(ThemePreference themePreference) {
        this.themePreference = themePreference;
    }

    public boolean isAdmin() {
        return Boolean.TRUE.equals(isAdmin);
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
    }

    public boolean isBanned() {
        return Boolean.TRUE.equals(banned);
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public String getBanReason() {
        return banReason;
    }

    public void setBanReason(String banReason) {
        this.banReason = banReason;
    }

    public LocalDateTime getBanExpiresAt() {
        return banExpiresAt;
    }

    public void setBanExpiresAt(LocalDateTime banExpiresAt) {
        this.banExpiresAt = banExpiresAt;
    }

    /** True if banned and the ban window hasn't expired yet. */
    public boolean isCurrentlyBanned() {
        if (!isBanned()) return false;
        return banExpiresAt == null || banExpiresAt.isAfter(LocalDateTime.now());
    }
}
