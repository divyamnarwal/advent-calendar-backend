package com.divyam.advent.service;

import com.divyam.advent.dto.AuthEnsureUserRequest;
import com.divyam.advent.enums.Culture;
import com.divyam.advent.exception.UserBannedException;
import com.divyam.advent.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(userService);
    }

    @Test
    void getCurrentUser_failsWhenJwtSubjectMissing() {
        Jwt jwt = jwt("");
        assertThrows(AccessDeniedException.class, () -> service.getCurrentUser(jwt));
    }

    @Test
    void getCurrentUser_failsWhenUserNotLinked() {
        Jwt jwt = jwt("clerk_user_1");
        when(userService.getByAuthSubject(eq("CLERK"), eq("clerk_user_1")))
                .thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class, () -> service.getCurrentUser(jwt));
    }

    @Test
    void getCurrentUser_returnsUserWhenNotBanned() {
        Jwt jwt = jwt("clerk_user_1");
        User u = new User(1L, "Ann", "ann@example.com");
        when(userService.getByAuthSubject(eq("CLERK"), eq("clerk_user_1")))
                .thenReturn(Optional.of(u));
        when(userService.reconcileBan(u)).thenReturn(u);

        User result = service.getCurrentUser(jwt);

        assertSame(u, result);
    }

    @Test
    void getCurrentUser_throwsUserBannedExceptionWhenActiveBan() {
        Jwt jwt = jwt("clerk_user_1");
        User banned = new User(1L, "Ann", "ann@example.com");
        banned.setBanned(true);
        banned.setBanReason("Inappropriate proof photo");
        banned.setBanExpiresAt(LocalDateTime.now().plusDays(3));
        when(userService.getByAuthSubject(eq("CLERK"), eq("clerk_user_1")))
                .thenReturn(Optional.of(banned));
        when(userService.reconcileBan(banned)).thenReturn(banned);

        UserBannedException ex = assertThrows(UserBannedException.class, () -> service.getCurrentUser(jwt));
        assertEquals("Inappropriate proof photo", ex.getReason());
        assertEquals(banned.getBanExpiresAt(), ex.getExpiresAt());
    }

    @Test
    void getCurrentUser_returnsAfterAutoUnbanWhenExpired() {
        Jwt jwt = jwt("clerk_user_1");
        User wasBanned = new User(1L, "Ann", "ann@example.com");
        // Simulate reconcile clearing the expired ban.
        wasBanned.setBanned(false);
        wasBanned.setBanReason(null);
        wasBanned.setBanExpiresAt(null);
        when(userService.getByAuthSubject(eq("CLERK"), eq("clerk_user_1")))
                .thenReturn(Optional.of(wasBanned));
        when(userService.reconcileBan(wasBanned)).thenReturn(wasBanned);

        User result = service.getCurrentUser(jwt);

        assertSame(wasBanned, result);
        assertFalse(result.isCurrentlyBanned());
    }

    @Test
    void ensureUser_throwsUserBannedExceptionDuringRelogin() {
        Jwt jwt = jwt("clerk_user_1");
        AuthEnsureUserRequest req = new AuthEnsureUserRequest();
        req.setEmail("ann@example.com");
        req.setName("Ann");
        req.setCountry(Culture.GLOBAL);

        User banned = new User(1L, "Ann", "ann@example.com");
        banned.setBanned(true);
        banned.setBanReason("Spam");
        banned.setBanExpiresAt(LocalDateTime.now().plusHours(1));
        when(userService.upsertAuthUser(
                eq("CLERK"), eq("clerk_user_1"), anyString(), anyString(), any(Culture.class)))
                .thenReturn(banned);
        when(userService.reconcileBan(banned)).thenReturn(banned);

        UserBannedException ex = assertThrows(UserBannedException.class, () -> service.ensureUser(jwt, req));
        assertEquals("Spam", ex.getReason());
    }

    private Jwt jwt(String subject) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("foo", "bar")
                .build();
    }
}
