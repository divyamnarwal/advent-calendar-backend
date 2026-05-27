package com.divyam.advent.enums;

/**
 * How a prize winner is ranked. The leaderboard is computed live per criterion so an
 * admin can review candidates before awarding (hybrid flow).
 */
public enum PrizeCriteria {

    /** Largest consecutive-day completion streak within the prize period. */
    LONGEST_STREAK,

    /** Most completed challenges within the prize period. */
    MOST_COMPLETED,

    /** Fastest to complete a specific challenge (by completion duration). Requires targetChallengeId. */
    FASTEST_CHALLENGE
}
