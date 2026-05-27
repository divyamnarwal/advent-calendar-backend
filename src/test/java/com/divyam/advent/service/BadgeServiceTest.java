package com.divyam.advent.service;

import com.divyam.advent.enums.CompletionStatus;
import com.divyam.advent.enums.EnergyLevel;
import com.divyam.advent.enums.ModerationStatus;
import com.divyam.advent.model.Challenge;
import com.divyam.advent.model.User;
import com.divyam.advent.model.UserChallenge;
import com.divyam.advent.repository.BadgeRepository;
import com.divyam.advent.repository.UserChallengeRepository;
import com.divyam.advent.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BadgeServiceTest {

    @Mock
    private BadgeRepository badgeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserChallengeRepository userChallengeRepository;

    private BadgeService service;

    @BeforeEach
    void setUp() {
        service = new BadgeService(badgeRepository, userRepository, userChallengeRepository);
        // Empty badge catalog — exercise stat code without bumping into criteria.
        when(badgeRepository.findAllByOrderByTitleAsc()).thenReturn(Collections.emptyList());
        // No ASSIGNED quests to sweep by default.
        when(userChallengeRepository.findByUser_IdAndStatus(anyLong(), eq(CompletionStatus.ASSIGNED)))
                .thenReturn(Collections.emptyList());
    }

    // --- streak ----------------------------------------------------------

    @Test
    void streak_zeroWhenNoApprovedCompletions() {
        User u = user(1L);
        stubApprovedCompletions(); // empty
        stubTodayQuests();         // none

        service.evaluateAndAssignBadges(u);

        assertEquals(0, u.getStreak());
    }

    @Test
    void streak_countsConsecutiveApprovedDaysEndingToday() {
        User u = user(1L);
        LocalDate today = LocalDate.now();
        stubApprovedCompletions(
                today.atTime(10, 0),
                today.minusDays(1).atTime(10, 0),
                today.minusDays(2).atTime(10, 0));
        stubTodayQuests();

        service.evaluateAndAssignBadges(u);

        assertEquals(3, u.getStreak());
    }

    @Test
    void streak_breakBetweenChainStopsCount() {
        User u = user(1L);
        LocalDate today = LocalDate.now();
        // gap at today-2 → chain from today is just today + yesterday
        stubApprovedCompletions(
                today.atTime(10, 0),
                today.minusDays(1).atTime(10, 0),
                today.minusDays(3).atTime(10, 0));
        stubTodayQuests();

        service.evaluateAndAssignBadges(u);

        assertEquals(2, u.getStreak());
    }

    @Test
    void streak_zeroWhenLatestApprovedIsOlderThanYesterday() {
        User u = user(1L);
        LocalDate today = LocalDate.now();
        stubApprovedCompletions(today.minusDays(3).atTime(12, 0));
        stubTodayQuests();

        service.evaluateAndAssignBadges(u);

        assertEquals(0, u.getStreak());
    }

    @Test
    void streak_immediateBreakWhenTodayExpired() {
        User u = user(1L);
        LocalDate today = LocalDate.now();
        // Yesterday was approved; today's quest expired with no approval.
        stubApprovedCompletions(today.minusDays(1).atTime(10, 0));
        UserChallenge todayExpired = new UserChallenge(u, smallChallenge(), CompletionStatus.EXPIRED);
        todayExpired.setStartTime(today.atTime(9, 0));
        stubTodayQuests(todayExpired);

        service.evaluateAndAssignBadges(u);

        assertEquals(0, u.getStreak());
    }

    @Test
    void streak_immediateBreakWhenTodayRejected() {
        User u = user(1L);
        LocalDate today = LocalDate.now();
        stubApprovedCompletions(today.minusDays(1).atTime(10, 0));
        UserChallenge todayRejected = new UserChallenge(u, smallChallenge(), CompletionStatus.COMPLETED);
        todayRejected.setStartTime(today.atTime(9, 0));
        todayRejected.setModerationStatus(ModerationStatus.REJECTED);
        stubTodayQuests(todayRejected);

        service.evaluateAndAssignBadges(u);

        assertEquals(0, u.getStreak());
    }

    @Test
    void streak_pendingTodayDoesNotBreakChain() {
        User u = user(1L);
        LocalDate today = LocalDate.now();
        // Yesterday approved + today still PENDING → chain shows 1 from yesterday;
        // streak doesn't bump to 2 until admin approves.
        stubApprovedCompletions(today.minusDays(1).atTime(10, 0));
        UserChallenge todayPending = new UserChallenge(u, smallChallenge(), CompletionStatus.COMPLETED);
        todayPending.setStartTime(today.atTime(9, 0));
        todayPending.setModerationStatus(ModerationStatus.PENDING);
        stubTodayQuests(todayPending);

        service.evaluateAndAssignBadges(u);

        assertEquals(1, u.getStreak());
    }

    // --- sweep + ELO floor ----------------------------------------------

    @Test
    void sweepExpired_flipsAssignedQuestsPastDeadlineToExpired() {
        User u = user(1L);
        Challenge c = smallChallenge();
        c.setDurationMinutes(30);
        UserChallenge stale = new UserChallenge(u, c, CompletionStatus.ASSIGNED);
        stale.setStartTime(LocalDateTime.now().minusHours(2));
        when(userChallengeRepository.findByUser_IdAndStatus(1L, CompletionStatus.ASSIGNED))
                .thenReturn(List.of(stale));
        stubApprovedCompletions();
        stubTodayQuests();

        service.evaluateAndAssignBadges(u);

        assertEquals(CompletionStatus.EXPIRED, stale.getStatus());
        verify(userChallengeRepository, atLeastOnce()).save(stale);
    }

    @Test
    void elo_neverDropsBelowExistingValue() {
        User u = user(1L);
        u.setElo(500L); // historical max
        stubApprovedCompletions(); // no current approved completions
        stubTodayQuests();
        when(userChallengeRepository
                .findByUser_IdAndStatusAndModerationStatus(1L, CompletionStatus.COMPLETED, ModerationStatus.APPROVED))
                .thenReturn(Collections.emptyList());

        service.evaluateAndAssignBadges(u);

        assertTrue(u.getElo() >= 500L, "ELO must never drop below historical max");
    }

    @Test
    void elo_growsFromApprovedCompletionsOnly() {
        User u = user(1L);
        u.setElo(0L);
        Challenge highEnergy = smallChallenge();
        highEnergy.setEnergyLevel(EnergyLevel.HIGH);
        UserChallenge approved = new UserChallenge(u, highEnergy, CompletionStatus.COMPLETED);
        approved.setModerationStatus(ModerationStatus.APPROVED);
        stubApprovedCompletions(LocalDate.now().atTime(10, 0));
        stubTodayQuests();
        when(userChallengeRepository
                .findByUser_IdAndStatusAndModerationStatus(1L, CompletionStatus.COMPLETED, ModerationStatus.APPROVED))
                .thenReturn(List.of(approved));

        service.evaluateAndAssignBadges(u);

        // Base ELO for HIGH (40) + 1-day streak bonus (5).
        assertEquals(45L, u.getElo());
    }

    // --- helpers ---------------------------------------------------------

    private User user(Long id) {
        User u = new User(id, "Ann", "ann@example.com");
        u.setStreak(0);
        u.setTotalPoints(0L);
        u.setElo(0L);
        return u;
    }

    private Challenge smallChallenge() {
        Challenge c = new Challenge();
        c.setId(1L);
        c.setEnergyLevel(EnergyLevel.MEDIUM);
        return c;
    }

    private void stubApprovedCompletions(LocalDateTime... times) {
        List<LocalDateTime> sorted = new ArrayList<>(List.of(times));
        sorted.sort((a, b) -> b.compareTo(a));
        when(userChallengeRepository.findApprovedCompletionTimesDesc(anyLong())).thenReturn(sorted);
        when(userChallengeRepository
                .countByUser_IdAndStatusAndModerationStatus(
                        anyLong(), eq(CompletionStatus.COMPLETED), eq(ModerationStatus.APPROVED)))
                .thenReturn((long) times.length);
    }

    private void stubTodayQuests(UserChallenge... ucs) {
        when(userChallengeRepository.findByUser_IdAndStartTimeAfter(anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(ucs));
    }
}
