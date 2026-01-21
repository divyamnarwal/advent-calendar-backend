package com.divyam.advent.enums;

/**
 * Represents a user's mood for the day.
 * Used to tailor challenge selection to the user's energy state.
 */
public enum Mood {

    /**
     * User is feeling low energy, tired, or down.
     * Best matched with LOW energy challenges (easy, achievable).
     */
    LOW,

    /**
     * User is feeling neutral, balanced, or average.
     * Best matched with MEDIUM energy challenges (balanced).
     */
    NEUTRAL,

    /**
     * User is feeling high energy, motivated, or excited.
     * Best matched with HIGH energy challenges (exciting, ambitious).
     */
    HIGH
}
