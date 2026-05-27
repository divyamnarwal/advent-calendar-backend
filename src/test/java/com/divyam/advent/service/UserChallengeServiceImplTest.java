package com.divyam.advent.service;

import com.divyam.advent.enums.ChallengeCategory;
import com.divyam.advent.enums.CompletionStatus;
import com.divyam.advent.enums.Culture;
import com.divyam.advent.enums.EnergyLevel;
import com.divyam.advent.enums.ModerationStatus;
import com.divyam.advent.enums.Mood;
import com.divyam.advent.exception.ChallengeExpiredException;
import com.divyam.advent.model.Challenge;
import com.divyam.advent.model.User;
import com.divyam.advent.model.UserChallenge;
import com.divyam.advent.repository.ChallengeRepository;
import com.divyam.advent.repository.UserChallengeRepository;
import com.divyam.advent.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserChallengeServiceImplTest {

    @Mock
    private UserChallengeRepository userChallengeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ChallengeRepository challengeRepository;
    @Mock
    private BadgeService badgeService;
    @Mock
    private ChallengeCycleSyncService challengeCycleSyncService;

    private UserChallengeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserChallengeServiceImpl(
                userChallengeRepository,
                userRepository,
                challengeRepository,
                badgeService,
                challengeCycleSyncService,
                "",
                ""
        );
    }

    // --- Photo-required completion -----------------------------------------

    @Test
    void markAsCompleted_setsProofAndCompletesWithPendingModeration() {
        User user = new User(1L, "Ann", "ann@example.com");
        UserChallenge uc = new UserChallenge(user, challenge(10L), CompletionStatus.ASSIGNED);
        uc.setId(7L);
        when(userChallengeRepository.findById(7L)).thenReturn(Optional.of(uc));
        when(userChallengeRepository.save(any(UserChallenge.class))).thenAnswer(i -> i.getArgument(0));

        UserChallenge result = service.markAsCompleted(7L, "https://img/x.jpg", "pub_x", "Walked 5km");

        assertEquals(CompletionStatus.COMPLETED, result.getStatus());
        // Proof uploads land in PENDING — admin must approve before the
        // completion counts toward streak / win rate / ELO.
        assertEquals(ModerationStatus.PENDING, result.getModerationStatus());
        assertEquals("https://img/x.jpg", result.getProofPhotoUrl());
        assertEquals("pub_x", result.getProofPhotoPublicId());
        assertNotNull(result.getCompletionTime());
        verify(badgeService).evaluateAndAssignBadges(user);
    }

    @Test
    void markAsCompleted_marksExpiredAndThrowsWhenPastAdminDeadline() {
        User user = new User(1L, "Ann", "ann@example.com");
        Challenge c = challenge(11L);
        c.setDurationMinutes(30);
        UserChallenge uc = new UserChallenge(user, c, CompletionStatus.ASSIGNED);
        uc.setId(9L);
        uc.setStartTime(LocalDateTime.now().minusHours(2));
        when(userChallengeRepository.findById(9L)).thenReturn(Optional.of(uc));
        when(userChallengeRepository.save(any(UserChallenge.class))).thenAnswer(i -> i.getArgument(0));

        assertThrows(ChallengeExpiredException.class,
                () -> service.markAsCompleted(9L, "https://img/x.jpg", "p", null));

        // Persist the loss + re-evaluate stats before throwing.
        assertEquals(CompletionStatus.EXPIRED, uc.getStatus());
        verify(userChallengeRepository).save(uc);
        verify(badgeService).evaluateAndAssignBadges(user);
    }

    @Test
    void markAsCompleted_marksExpiredWhenStartedYesterday() {
        User user = new User(1L, "Ann", "ann@example.com");
        UserChallenge uc = new UserChallenge(user, challenge(12L), CompletionStatus.ASSIGNED);
        uc.setId(10L);
        // No admin duration set — fallback deadline = next midnight after start.
        // Starting "yesterday" means now is past that deadline.
        uc.setStartTime(LocalDateTime.now().minusDays(1));
        when(userChallengeRepository.findById(10L)).thenReturn(Optional.of(uc));
        when(userChallengeRepository.save(any(UserChallenge.class))).thenAnswer(i -> i.getArgument(0));

        assertThrows(ChallengeExpiredException.class,
                () -> service.markAsCompleted(10L, "https://img/x.jpg", "p", null));
        assertEquals(CompletionStatus.EXPIRED, uc.getStatus());
    }

    @Test
    void markAsCompleted_succeedsWithinAdminDeadline() {
        User user = new User(1L, "Ann", "ann@example.com");
        Challenge c = challenge(13L);
        c.setDurationMinutes(60);
        UserChallenge uc = new UserChallenge(user, c, CompletionStatus.ASSIGNED);
        uc.setId(11L);
        uc.setStartTime(LocalDateTime.now().minusMinutes(5));
        when(userChallengeRepository.findById(11L)).thenReturn(Optional.of(uc));
        when(userChallengeRepository.save(any(UserChallenge.class))).thenAnswer(i -> i.getArgument(0));

        UserChallenge result = service.markAsCompleted(11L, "https://img/x.jpg", "p", null);

        assertEquals(CompletionStatus.COMPLETED, result.getStatus());
        assertEquals(ModerationStatus.PENDING, result.getModerationStatus());
    }

    @Test
    void markAsCompleted_rejectsBlankPhoto() {
        User user = new User(1L, "Ann", "ann@example.com");
        UserChallenge uc = new UserChallenge(user, challenge(10L), CompletionStatus.ASSIGNED);
        uc.setId(7L);
        when(userChallengeRepository.findById(7L)).thenReturn(Optional.of(uc));

        assertThrows(IllegalArgumentException.class, () -> service.markAsCompleted(7L, "   ", null, null));
        verify(userChallengeRepository, never()).save(any());
        verify(badgeService, never()).evaluateAndAssignBadges(any());
    }

    @Test
    void markAsCompleted_rejectsNonAssigned() {
        User user = new User(1L, "Ann", "ann@example.com");
        UserChallenge uc = new UserChallenge(user, challenge(10L), CompletionStatus.COMPLETED);
        uc.setId(7L);
        when(userChallengeRepository.findById(7L)).thenReturn(Optional.of(uc));

        assertThrows(IllegalStateException.class, () -> service.markAsCompleted(7L, "https://img/x.jpg", "p", null));
    }

    @Test
    void updateStatus_toCompletedRequiresProofPhoto() {
        User user = new User(1L, "Ann", "ann@example.com");
        UserChallenge uc = new UserChallenge(user, challenge(10L), CompletionStatus.ASSIGNED);
        uc.setId(7L); // no proof photo set
        when(userChallengeRepository.findById(7L)).thenReturn(Optional.of(uc));

        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(7L, CompletionStatus.COMPLETED));
        verify(userChallengeRepository, never()).save(any());
    }

    @Test
    void updateStatus_toCompletedSucceedsWhenProofPresent() {
        User user = new User(1L, "Ann", "ann@example.com");
        UserChallenge uc = new UserChallenge(user, challenge(10L), CompletionStatus.ASSIGNED);
        uc.setId(7L);
        uc.setProofPhotoUrl("https://img/x.jpg");
        when(userChallengeRepository.findById(7L)).thenReturn(Optional.of(uc));
        when(userChallengeRepository.save(any(UserChallenge.class))).thenAnswer(i -> i.getArgument(0));

        UserChallenge result = service.updateStatus(7L, CompletionStatus.COMPLETED);

        assertEquals(CompletionStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getCompletionTime());
        verify(badgeService).evaluateAndAssignBadges(user);
    }

    // --- Date-bound (holiday) challenge selection --------------------------

    @Test
    void previewDailyChallenge_returnsHolidayChallengeOnItsDay() {
        User user = new User(1L, "Ann", "ann@example.com"); // GLOBAL
        LocalDate today = LocalDate.now();
        Challenge holiday = challenge(50L);
        holiday.setEventMonth(today.getMonthValue());
        holiday.setEventDay(today.getDayOfMonth());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userChallengeRepository.findByUser_IdAndStartTimeAfter(
                eq(1L), any(LocalDateTime.class))).thenReturn(List.of());
        when(challengeRepository.findByActiveTrueAndEventMonthAndEventDay(
                today.getMonthValue(), today.getDayOfMonth())).thenReturn(List.of(holiday));

        Challenge result = service.previewDailyChallenge(1L, Mood.NEUTRAL);

        assertEquals(50L, result.getId());
    }

    @Test
    void previewDailyChallenge_holidayFallsBackAcrossCulture() {
        User user = new User(1L, "Ann", "ann@example.com"); // GLOBAL user
        LocalDate today = LocalDate.now();
        Challenge holiday = challenge(51L);
        holiday.setCulture(Culture.RUSSIA); // no GLOBAL match -> fallback should still pick it
        holiday.setEventMonth(today.getMonthValue());
        holiday.setEventDay(today.getDayOfMonth());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userChallengeRepository.findByUser_IdAndStartTimeAfter(
                eq(1L), any(LocalDateTime.class))).thenReturn(List.of());
        when(challengeRepository.findByActiveTrueAndEventMonthAndEventDay(
                today.getMonthValue(), today.getDayOfMonth())).thenReturn(List.of(holiday));

        Challenge result = service.previewDailyChallenge(1L, Mood.NEUTRAL);

        assertEquals(51L, result.getId());
    }

    @Test
    void previewDailyChallenge_excludesDateBoundChallengeOnNonHolidayDay() {
        User user = new User(1L, "Ann", "ann@example.com");
        LocalDate today = LocalDate.now();

        Challenge dateBound = challenge(60L);
        dateBound.setEventMonth(12);
        dateBound.setEventDay(25); // a holiday, but not selectable today
        Challenge normal = challenge(61L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userChallengeRepository.findByUser_IdAndStartTimeAfter(
                eq(1L), any(LocalDateTime.class))).thenReturn(List.of());
        when(challengeRepository.findByActiveTrueAndEventMonthAndEventDay(
                today.getMonthValue(), today.getDayOfMonth())).thenReturn(List.of());
        when(userChallengeRepository.findByUser_Id(1L)).thenReturn(List.of());
        when(challengeRepository.findByEnergyLevelAndActiveTrue(EnergyLevel.MEDIUM))
                .thenReturn(List.of(dateBound, normal));

        Challenge result = service.previewDailyChallenge(1L, Mood.NEUTRAL);

        assertEquals(61L, result.getId());
    }

    private Challenge challenge(Long id) {
        Challenge c = new Challenge();
        c.setId(id);
        c.setTitle("Challenge " + id);
        c.setDescription("desc");
        c.setCategory(ChallengeCategory.WILDCARD);
        c.setEnergyLevel(EnergyLevel.MEDIUM);
        c.setActive(true);
        c.setCulture(Culture.GLOBAL);
        return c;
    }
}
