package com.divyam.advent.service;

import com.divyam.advent.dto.ProfileUpdateRequestDto;
import com.divyam.advent.dto.PublicProfileDto;
import com.divyam.advent.enums.CompletionStatus;
import com.divyam.advent.enums.Culture;
import com.divyam.advent.enums.ModerationStatus;
import com.divyam.advent.model.User;
import com.divyam.advent.repository.UserChallengeRepository;
import com.divyam.advent.repository.UserRepository;
import com.divyam.advent.security.AdminGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProfileServiceTest {

    @Mock
    private AuthService authService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BadgeService badgeService;
    @Mock
    private UserChallengeRepository userChallengeRepository;
    @Mock
    private AdminGuard adminGuard;

    private ProfileService service;

    @BeforeEach
    void setUp() {
        // cloud-name "advent-test" → only https://res.cloudinary.com/advent-test/… is accepted
        service = new ProfileService(authService, userRepository, badgeService,
                userChallengeRepository, adminGuard, "advent-test");
        when(badgeService.evaluateAndAssignBadges(any(User.class))).thenReturn(Collections.emptyList());
        when(badgeService.getEarnedBadgeIds(any(User.class))).thenReturn(Collections.emptyList());
        when(badgeService.getAllBadges()).thenReturn(Collections.emptyList());
    }

    // --- avatar/banner URL validation -----------------------------------

    @Test
    void updateProfile_rejectsArbitraryAvatarUrl() {
        User u = userInRepo();
        ProfileUpdateRequestDto req = new ProfileUpdateRequestDto();
        req.setAvatar("https://evil.example.com/pic.jpg");

        Jwt jwt = jwt("clerk_user_1");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateProfile(jwt, req));
        org.junit.jupiter.api.Assertions.assertTrue(ex.getMessage().toLowerCase().contains("avatar"));
        assertNull(u.getAvatar(), "Avatar must not be set when validation rejects");
    }

    @Test
    void updateProfile_rejectsArbitraryBannerUrl() {
        User u = userInRepo();
        ProfileUpdateRequestDto req = new ProfileUpdateRequestDto();
        req.setBannerUrl("https://random.cdn/banner.png");

        Jwt jwt = jwt("clerk_user_1");
        assertThrows(IllegalArgumentException.class, () -> service.updateProfile(jwt, req));
        assertNull(u.getBannerUrl());
    }

    @Test
    void updateProfile_acceptsCloudinaryAvatarFromConfiguredCloud() {
        User u = userInRepo();
        ProfileUpdateRequestDto req = new ProfileUpdateRequestDto();
        req.setAvatar("https://res.cloudinary.com/advent-test/image/upload/v1/abc.jpg");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        Jwt jwt = jwt("clerk_user_1");
        service.updateProfile(jwt, req);

        assertEquals("https://res.cloudinary.com/advent-test/image/upload/v1/abc.jpg", u.getAvatar());
    }

    // --- country swap --------------------------------------------------

    @Test
    void updateProfile_updatesCountry() {
        User u = userInRepo();
        u.setCountry(Culture.GLOBAL);
        ProfileUpdateRequestDto req = new ProfileUpdateRequestDto();
        req.setCountry("RUSSIA");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        Jwt jwt = jwt("clerk_user_1");
        service.updateProfile(jwt, req);

        assertEquals(Culture.RUSSIA, u.getCountry());
    }

    // --- public profile win rate --------------------------------------

    @Test
    void getPublicProfile_winRateBalancesApprovedAgainstRejectedAndExpired() {
        User u = new User(7L, "Bob", "bob@example.com");
        u.setStreak(0);
        u.setTotalPoints(0L);
        u.setElo(100L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(u));
        when(userChallengeRepository.countByUser_IdAndStatusAndModerationStatus(
                eq(7L), eq(CompletionStatus.COMPLETED), eq(ModerationStatus.APPROVED)))
                .thenReturn(6L);
        when(userChallengeRepository.countByUser_IdAndStatusAndModerationStatus(
                eq(7L), eq(CompletionStatus.COMPLETED), eq(ModerationStatus.REJECTED)))
                .thenReturn(2L);
        when(userChallengeRepository.countByUser_IdAndStatus(eq(7L), eq(CompletionStatus.EXPIRED)))
                .thenReturn(2L);

        PublicProfileDto dto = service.getPublicProfile(7L);

        // approved=6, fails=4 → 6 / 10 = 0.6
        assertNotNull(dto.winRate());
        assertEquals(0.6, dto.winRate(), 0.0001);
        assertEquals(6L, dto.completedCount());
        assertEquals(4L, dto.failedCount());
    }

    @Test
    void getPublicProfile_winRateNullWhenNoSettledQuests() {
        User u = new User(8L, "Cara", "cara@example.com");
        u.setStreak(0);
        u.setTotalPoints(0L);
        u.setElo(0L);
        when(userRepository.findById(8L)).thenReturn(Optional.of(u));
        when(userChallengeRepository.countByUser_IdAndStatusAndModerationStatus(
                anyLong(), any(CompletionStatus.class), any(ModerationStatus.class)))
                .thenReturn(0L);
        when(userChallengeRepository.countByUser_IdAndStatus(anyLong(), any(CompletionStatus.class)))
                .thenReturn(0L);

        PublicProfileDto dto = service.getPublicProfile(8L);

        assertNull(dto.winRate());
        assertEquals(0L, dto.completedCount());
        assertEquals(0L, dto.failedCount());
    }

    // --- helpers ---------------------------------------------------------

    private User userInRepo() {
        User u = new User(1L, "Ann", "ann@example.com");
        u.setStreak(0);
        u.setTotalPoints(0L);
        u.setElo(0L);
        when(authService.getCurrentUser(any(Jwt.class))).thenReturn(u);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(u));
        when(userChallengeRepository.countByUser_IdAndStatusAndModerationStatus(
                anyLong(), any(CompletionStatus.class), any(ModerationStatus.class)))
                .thenReturn(0L);
        when(userChallengeRepository.countByUser_IdAndStatus(anyLong(), any(CompletionStatus.class)))
                .thenReturn(0L);
        return u;
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
