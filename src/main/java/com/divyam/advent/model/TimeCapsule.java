package com.divyam.advent.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a time capsule message stored by a user.
 * The message can only be retrieved after the reveal date has passed.
 */
@Entity
@Table(name = "time_capsules")
public class TimeCapsule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The ID of the user who created this capsule.
     * Users can only see their own capsules.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * The text content of the message.
     */
    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    /**
     * The date and time when this capsule can be revealed.
     * Before this time, the capsule is locked.
     */
    @Column(name = "reveal_date", nullable = false)
    private LocalDateTime revealDate;

    /**
     * When the capsule was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Whether the capsule has been revealed (viewed after reveal date).
     */
    @Column(name = "revealed", nullable = false)
    private boolean revealed = false;

    public TimeCapsule() {
    }

    /**
     * Constructor to create a new time capsule.
     * Automatically sets created timestamp to now.
     *
     * @param userId the ID of the user creating the capsule
     * @param content the message content
     * @param revealDate when the capsule can be opened
     */
    public TimeCapsule(Long userId, String content, LocalDateTime revealDate) {
        this.userId = userId;
        this.content = content;
        this.revealDate = revealDate;
        this.createdAt = LocalDateTime.now();
        this.revealed = false;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getRevealDate() {
        return revealDate;
    }

    public void setRevealDate(LocalDateTime revealDate) {
        this.revealDate = revealDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    /**
     * Check if the capsule can be opened based on current time.
     *
     * @return true if reveal date has passed
     */
    public boolean isRevealable() {
        return LocalDateTime.now().isAfter(revealDate) || LocalDateTime.now().equals(revealDate);
    }
}
