package com.divyam.advent.enums;

/**
 * Represents the completion status of a user's challenge participation.
 */
public enum CompletionStatus {
    /**
     * Challenge has been assigned to the user.
     */
    ASSIGNED,

    /**
     * User has successfully completed the challenge.
     */
    COMPLETED,

    /**
     * Deadline passed without completion. Final state — no retries. Counts as a
     * loss against win rate and breaks the streak; ELO is untouched.
     */
    EXPIRED
}
