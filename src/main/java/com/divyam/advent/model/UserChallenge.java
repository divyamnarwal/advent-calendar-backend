package com.divyam.advent.model;

import com.divyam.advent.enums.CompletionStatus;
import com.divyam.advent.enums.ModerationStatus;
import com.divyam.advent.enums.Mood;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Represents a user's participation in a challenge.
 * This is the "middle table" that connects Users and Challenges.
 *
 * Each row in this table = one user doing one challenge.
 */
@Entity
@Table(name = "user_challenges")
public class UserChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who is participating in this challenge.
     * Many UserChallenges can belong to ONE User.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The challenge the user is participating in.
     * Many UserChallenges can belong to ONE Challenge.
     */
    @ManyToOne
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    /**
     * Current status of this user's challenge participation.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CompletionStatus status;

    /**
     * When the user started this challenge.
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * When the user completed this challenge (null if not completed).
     */
    @Column(name = "completion_time")
    private LocalDateTime completionTime;

    /**
     * The user's mood when this challenge was assigned.
     * Used to tailor challenges to the user's energy state.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "mood", nullable = true)
    private Mood mood;

    /**
     * URL of the proof photo uploaded when completing this challenge.
     * A proof photo is mandatory to move a challenge to COMPLETED.
     */
    @Column(name = "proof_photo_url", length = 1024)
    private String proofPhotoUrl;

    /**
     * Cloudinary public id of the proof photo (used for cleanup / reference).
     */
    @Column(name = "proof_photo_public_id")
    private String proofPhotoPublicId;

    /**
     * Free-form note from the user describing what they did. Shown to admins
     * in the moderation feed alongside the proof photo.
     */
    @Column(name = "user_reflection", length = 2000)
    private String userReflection;

    /**
     * Admin moderation state. Defaults to OK on creation; admins flip to
     * FLAGGED or HIDDEN via /admin endpoints (post-approval model).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", nullable = false)
    private ModerationStatus moderationStatus = ModerationStatus.PENDING;

    /**
     * Default constructor required by JPA.
     */
    public UserChallenge() {
    }

    /**
     * Constructor to create a new UserChallenge.
     * Automatically sets start time to now.
     */
    public UserChallenge(User user, Challenge challenge, CompletionStatus status) {
        this.user = user;
        this.challenge = challenge;
        this.status = status;
        this.startTime = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public CompletionStatus getStatus() {
        return status;
    }

    public void setStatus(CompletionStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(LocalDateTime completionTime) {
        this.completionTime = completionTime;
    }

    public Mood getMood() {
        return mood;
    }

    public void setMood(Mood mood) {
        this.mood = mood;
    }

    public String getProofPhotoUrl() {
        return proofPhotoUrl;
    }

    public void setProofPhotoUrl(String proofPhotoUrl) {
        this.proofPhotoUrl = proofPhotoUrl;
    }

    public String getProofPhotoPublicId() {
        return proofPhotoPublicId;
    }

    public void setProofPhotoPublicId(String proofPhotoPublicId) {
        this.proofPhotoPublicId = proofPhotoPublicId;
    }

    public String getUserReflection() {
        return userReflection;
    }

    public void setUserReflection(String userReflection) {
        this.userReflection = userReflection;
    }

    public ModerationStatus getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(ModerationStatus moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    /**
     * Deadline by which this assignment must be completed. If the challenge has
     * an admin-set {@code durationMinutes}, deadline = startTime + duration.
     * Otherwise the default is "until the next day rolls over" — the midnight
     * after the start day. Returns null if startTime is missing.
     */
    public LocalDateTime getEffectiveDeadline() {
        if (startTime == null) {
            return null;
        }
        Integer limit = challenge != null ? challenge.getDurationMinutes() : null;
        if (limit != null) {
            return startTime.plusMinutes(limit);
        }
        return startTime.toLocalDate().plusDays(1).atStartOfDay();
    }
}
