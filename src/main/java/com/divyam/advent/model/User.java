package com.divyam.advent.model;

import com.divyam.advent.enums.Culture;
import com.divyam.advent.enums.ThemePreference;
import jakarta.persistence.*;

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

    @Column(name = "streak", nullable = false)
    private Integer streak = 0;

    @Column(name = "total_points", nullable = false)
    private Long totalPoints = 0L;

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
}
