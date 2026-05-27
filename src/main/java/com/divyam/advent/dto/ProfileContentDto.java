package com.divyam.advent.dto;

/**
 * Compact view used by the admin "profile content" moderation tab:
 * everyone who currently has an avatar and/or banner uploaded.
 */
public record ProfileContentDto(
        Long userId,
        String name,
        String email,
        String avatar,
        String bannerUrl
) {}
