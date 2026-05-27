package com.divyam.advent.exception;

import java.time.LocalDateTime;

/**
 * Thrown by AuthService when a banned user touches any authenticated endpoint
 * (or hits /auth/ensure-user during re-login). The dedicated handler returns
 * HTTP 403 with a stable {@code USER_BANNED} code plus the reason and expiry
 * so the frontend can render the ban screen without string-matching messages.
 */
public class UserBannedException extends RuntimeException {

    private final String reason;
    private final LocalDateTime expiresAt;

    public UserBannedException(String reason, LocalDateTime expiresAt) {
        super(reason != null ? reason : "Account banned");
        this.reason = reason;
        this.expiresAt = expiresAt;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
