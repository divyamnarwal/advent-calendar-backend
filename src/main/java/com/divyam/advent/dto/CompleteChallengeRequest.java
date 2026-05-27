package com.divyam.advent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for completing a challenge. A proof photo is mandatory:
 * the challenge cannot be marked COMPLETED without it. An optional
 * userReflection lets the user describe what they did; it is shown to
 * admins in the moderation feed.
 */
public class CompleteChallengeRequest {

    @NotBlank(message = "A proof photo is required to complete this challenge")
    private String photoUrl;

    private String photoPublicId;

    @Size(max = 2000, message = "Reflection cannot exceed 2000 characters")
    private String userReflection;

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhotoPublicId() {
        return photoPublicId;
    }

    public void setPhotoPublicId(String photoPublicId) {
        this.photoPublicId = photoPublicId;
    }

    public String getUserReflection() {
        return userReflection;
    }

    public void setUserReflection(String userReflection) {
        this.userReflection = userReflection;
    }
}
