package com.divyam.advent.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

/**
 * Represents a cultural context for challenges.
 * Challenges can be specific to a country or global (applicable to everyone).
 */
public enum Culture {

    /**
     * India-specific challenges.
     */
    INDIA,

    /**
     * Russia-specific challenges.
     */
    RUSSIA,

    /**
     * Global challenges applicable to all users regardless of country.
     */
    GLOBAL
    ;

    @JsonCreator
    public static Culture fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return GLOBAL;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "INDIA", "IN", "IND" -> INDIA;
            case "RUSSIA", "RU", "RUS" -> RUSSIA;
            case "GLOBAL", "WORLD", "ALL" -> GLOBAL;
            default -> throw new IllegalArgumentException("Invalid culture value: " + value);
        };
    }
}
