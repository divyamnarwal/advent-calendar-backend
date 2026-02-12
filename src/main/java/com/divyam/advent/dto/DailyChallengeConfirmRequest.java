package com.divyam.advent.dto;

import com.divyam.advent.enums.Mood;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for confirming a daily challenge after preview.
 */
public class DailyChallengeConfirmRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long challengeId;

    @NotNull
    private Mood mood;

    public DailyChallengeConfirmRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(Long challengeId) {
        this.challengeId = challengeId;
    }

    public Mood getMood() {
        return mood;
    }

    public void setMood(Mood mood) {
        this.mood = mood;
    }
}
