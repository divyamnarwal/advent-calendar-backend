package com.divyam.advent.service;

import com.divyam.advent.dto.AuthEnsureUserRequest;
import com.divyam.advent.enums.Culture;
import com.divyam.advent.exception.UserBannedException;
import com.divyam.advent.model.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String AUTH_PROVIDER_CLERK = "CLERK";

    private final UserService userService;

    public AuthService(UserService userService) {
        this.userService = userService;
    }

    public User ensureUser(Jwt jwt, AuthEnsureUserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        User user = userService.upsertAuthUser(
                AUTH_PROVIDER_CLERK,
                getSubject(jwt),
                request.getEmail(),
                request.getName(),
                request.getCountry() != null ? request.getCountry() : Culture.GLOBAL
        );
        return enforceNotBanned(user);
    }

    public User getCurrentUser(Jwt jwt) {
        User user = userService.getByAuthSubject(AUTH_PROVIDER_CLERK, getSubject(jwt))
                .orElseThrow(() -> new AccessDeniedException("Authenticated user is not linked yet"));
        return enforceNotBanned(user);
    }

    /**
     * Auto-lifts expired bans, then throws {@link UserBannedException} if the
     * user is still under an active ban. Called on every authenticated entry
     * point so a banned user can't touch any endpoint until the ban ends.
     */
    private User enforceNotBanned(User user) {
        User reconciled = userService.reconcileBan(user);
        if (reconciled.isCurrentlyBanned()) {
            throw new UserBannedException(reconciled.getBanReason(), reconciled.getBanExpiresAt());
        }
        return reconciled;
    }

    public void validateUserAccess(Jwt jwt, Long userId) {
        User currentUser = getCurrentUser(jwt);
        if (!currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You can only access your own data");
        }
    }

    private String getSubject(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().trim().isEmpty()) {
            throw new AccessDeniedException("Invalid authentication token");
        }
        return jwt.getSubject().trim();
    }
}
