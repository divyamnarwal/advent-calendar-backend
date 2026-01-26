package com.divyam.advent.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for time capsule response.
 * Contains capsule information without exposing user identifiers.
 */
public class TimeCapsuleResponseDto {

    private Long id;
    private String content;
    private LocalDateTime revealDate;
    private LocalDateTime createdAt;
    private boolean revealed;
    private boolean revealable;  // Whether it can be opened now

    public TimeCapsuleResponseDto() {
    }

    public TimeCapsuleResponseDto(Long id, String content, LocalDateTime revealDate,
                                   LocalDateTime createdAt, boolean revealed, boolean revealable) {
        this.id = id;
        this.content = content;
        this.revealDate = revealDate;
        this.createdAt = createdAt;
        this.revealed = revealed;
        this.revealable = revealable;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isRevealable() {
        return revealable;
    }

    public void setRevealable(boolean revealable) {
        this.revealable = revealable;
    }
}
