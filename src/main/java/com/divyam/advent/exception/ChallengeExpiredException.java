package com.divyam.advent.exception;

/**
 * Thrown when a user tries to complete a time-limited challenge after its
 * deadline (startTime + Challenge.durationMinutes). The dedicated handler
 * surfaces a stable {@code CHALLENGE_EXPIRED} error code so the frontend
 * can flip to an "out of time" UI without string-matching messages.
 */
public class ChallengeExpiredException extends RuntimeException {

    public ChallengeExpiredException(String message) {
        super(message);
    }
}
