package com.divyam.advent.service;

import com.divyam.advent.dto.AuthEnsureUserRequest;
import com.divyam.advent.enums.Culture;
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
        return userService.upsertAuthUser(
                AUTH_PROVIDER_CLERK,
                getSubject(jwt),
                request.getEmail(),
                request.getName(),
                request.getCountry() != null ? request.getCountry() : Culture.GLOBAL
        );
    }

    public User getCurrentUser(Jwt jwt) {
        return userService.getByAuthSubject(AUTH_PROVIDER_CLERK, getSubject(jwt))
                .orElseThrow(() -> new AccessDeniedException("Authenticated user is not linked yet"));
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
