package com.divyam.advent.security;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.divyam.advent.model.User;
import com.divyam.advent.service.UserService;

/**
 * Admin authorization.
 *
 * <p>Two tiers:
 * <ul>
 *   <li><b>Super-admins</b> — bootstrapped from the {@code ADMIN_CLERK_USER_ID} env var
 *       (comma-separated Clerk user ids). They cannot be revoked through the UI and may
 *       grant/revoke admin rights for others.</li>
 *   <li><b>Admins</b> — regular users flagged with {@code is_admin} in the database,
 *       managed at runtime by a super-admin.</li>
 * </ul>
 * Effective admin = super-admin OR db {@code is_admin}.
 */
@Component
public class AdminGuard {

    private static final String AUTH_PROVIDER_CLERK = "CLERK";

    /** Comma-separated list of Clerk user ids that are super-admins (a single id also works). */
    @Value("${admin.clerk-user-id:}")
    private String superAdminClerkUserIds;

    private final UserService userService;

    public AdminGuard(UserService userService) {
        this.userService = userService;
    }

    public boolean isSuperAdmin(String clerkId) {
        return clerkId != null && !clerkId.isBlank() && superAdminIds().contains(clerkId);
    }

    public boolean isAdmin(String clerkId) {
        if (isSuperAdmin(clerkId)) {
            return true;
        }
        if (clerkId == null || clerkId.isBlank()) {
            return false;
        }
        return userService.getByAuthSubject(AUTH_PROVIDER_CLERK, clerkId)
                .map(User::isAdmin)
                .orElse(false);
    }

    public void requireAdmin(String callerClerkId) {
        if (!isAdmin(callerClerkId)) {
            throw new AccessDeniedException("Admin access required");
        }
    }

    public void requireSuperAdmin(String callerClerkId) {
        if (!isSuperAdmin(callerClerkId)) {
            throw new AccessDeniedException("Super-admin access required");
        }
    }

    private List<String> superAdminIds() {
        if (superAdminClerkUserIds == null || superAdminClerkUserIds.isBlank()) {
            return List.of();
        }
        return Arrays.stream(superAdminClerkUserIds.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toList());
    }
}
