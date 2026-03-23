package com.divyam.advent.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class AdminGuard {

    @Value("${admin.clerk-user-id}")
    private String adminClerkUserId;

    public void requireAdmin(String callerClerkId) {
        if (adminClerkUserId == null || adminClerkUserId.isBlank()
                || !adminClerkUserId.equals(callerClerkId)) {
            throw new AccessDeniedException("Admin access required");
        }
    }
}
