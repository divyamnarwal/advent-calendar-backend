package com.divyam.advent.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for creating a new time capsule.
 */
public class TimeCapsuleRequestDto {

    /**
     * The text content of the message.
     */
    private String content;

    /**
     * Optional: specific reveal date.
     * If not provided, daysUntilReveal must be specified.
     */
    private LocalDateTime revealDate;

    /**
     * Optional: number of days from now to reveal.
     * If not provided, revealDate must be specified.
     * Defaults to 7 days if neither is provided.
     */
    private Integer daysUntilReveal;

    public TimeCapsuleRequestDto() {
    }

    public TimeCapsuleRequestDto(String content, LocalDateTime revealDate, Integer daysUntilReveal) {
        this.content = content;
        this.revealDate = revealDate;
        this.daysUntilReveal = daysUntilReveal;
    }

    // Getters and Setters

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

    public Integer getDaysUntilReveal() {
        return daysUntilReveal;
    }

    public void setDaysUntilReveal(Integer daysUntilReveal) {
        this.daysUntilReveal = daysUntilReveal;
    }
}
