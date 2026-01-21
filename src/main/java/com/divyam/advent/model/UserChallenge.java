package com.divyam.advent.model;

import com.divyam.advent.enums.CompletionStatus;
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
}
