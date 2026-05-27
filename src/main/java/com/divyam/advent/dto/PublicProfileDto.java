package com.divyam.advent.dto;

import java.util.List;

/** Another user's public profile — no email or other private data. */
public record PublicProfileDto(
        Long id,
        String name,
        String country,
        String avatar,
        String bio,
        String accentColor,
        String bannerUrl,
        String featuredBadgeId,
        String featuredBadgeTitle,
        String featuredBadgeIcon,
        SocialLinksDto socials,
        long elo,
        int level,
        long levelMinElo,
        Long levelMaxElo,
        int streak,
        long completedCount,
        long failedCount,
        Double winRate,
        List<ProfileBadgeDto> badges,
        boolean isAdmin,
        boolean isSuperAdmin
) {}
