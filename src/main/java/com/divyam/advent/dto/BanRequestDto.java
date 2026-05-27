package com.divyam.advent.dto;

/**
 * Admin payload to ban a user: free-text {@code reason} (optional, shown to
 * the user on the ban screen) and {@code durationDays} (optional; null =
 * permanent until manually revoked).
 */
public class BanRequestDto {
    private String reason;
    private Integer durationDays;

    public BanRequestDto() {}

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }
}
