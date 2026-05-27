package com.divyam.advent.service;

import com.divyam.advent.enums.CompletionStatus;
import com.divyam.advent.enums.EnergyLevel;
import com.divyam.advent.enums.ModerationStatus;
import com.divyam.advent.enums.ThemePreference;
import com.divyam.advent.model.Badge;
import com.divyam.advent.model.User;
import com.divyam.advent.model.UserChallenge;
import com.divyam.advent.repository.BadgeRepository;
import com.divyam.advent.repository.UserChallengeRepository;
import com.divyam.advent.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BadgeService {

    private static final long POINTS_PER_COMPLETED_CHALLENGE = 10L;
    private static final String CRITERIA_STREAK_DAYS = "STREAK_DAYS";
    private static final String CRITERIA_COMPLETED_CHALLENGES = "COMPLETED_CHALLENGES";

    // ELO earned per completed quest by difficulty, plus a small per-day current-streak bonus.
    private static final long ELO_LOW = 15L;
    private static final long ELO_MEDIUM = 25L;
    private static final long ELO_HIGH = 40L;
    private static final long ELO_STREAK_BONUS_PER_DAY = 5L;

    private final BadgeRepository badgeRepository;
    private final UserRepository userRepository;
    private final UserChallengeRepository userChallengeRepository;

    public BadgeService(
            BadgeRepository badgeRepository,
            UserRepository userRepository,
            UserChallengeRepository userChallengeRepository
    ) {
        this.badgeRepository = badgeRepository;
        this.userRepository = userRepository;
        this.userChallengeRepository = userChallengeRepository;
    }

    public List<Badge> getAllBadges() {
        return badgeRepository.findAllByOrderByTitleAsc();
    }

    @Transactional
    public List<Badge> evaluateAndAssignBadges(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Valid user is required for badge evaluation");
        }

        sweepExpiredChallenges(user.getId());

        // Only APPROVED completions feed stats — PENDING is in flight,
        // REJECTED is treated as a loss. Mirrors the win-rate definition.
        long completedChallenges = userChallengeRepository.countByUser_IdAndStatusAndModerationStatus(
                user.getId(),
                CompletionStatus.COMPLETED,
                ModerationStatus.APPROVED
        );
        int currentStreak = calculateCurrentStreak(user.getId());
        long totalPoints = completedChallenges * POINTS_PER_COMPLETED_CHALLENGE;

        Set<String> existingBadgeIds = user.getBadges() != null
                ? new LinkedHashSet<>(user.getBadges())
                : new LinkedHashSet<>();
        List<Badge> catalog = getAllBadges();
        List<Badge> newlyUnlocked = new ArrayList<>();

        for (Badge badge : catalog) {
            if (!existingBadgeIds.contains(badge.getId())
                    && doesUserMeetCriteria(badge.getCriteria(), completedChallenges, currentStreak)) {
                existingBadgeIds.add(badge.getId());
                newlyUnlocked.add(badge);
            }
        }

        boolean changed = false;

        if (!Objects.equals(user.getStreak(), currentStreak)) {
            user.setStreak(currentStreak);
            changed = true;
        }

        if (!Objects.equals(user.getTotalPoints(), totalPoints)) {
            user.setTotalPoints(totalPoints);
            changed = true;
        }

        // ELO only grows: never let a streak reset lower a user's rating.
        long computedElo = computeElo(user.getId(), currentStreak);
        long elo = Math.max(user.getElo(), computedElo);
        if (user.getElo() != elo) {
            user.setElo(elo);
            changed = true;
        }

        if (!Objects.equals(user.getBadges(), existingBadgeIds)) {
            user.setBadges(existingBadgeIds);
            changed = true;
        }

        if (user.getThemePreference() == null) {
            user.setThemePreference(ThemePreference.SYSTEM);
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
        }

        return newlyUnlocked;
    }

    private long computeElo(Long userId, int currentStreak) {
        List<UserChallenge> approved = userChallengeRepository
                .findByUser_IdAndStatusAndModerationStatus(
                        userId, CompletionStatus.COMPLETED, ModerationStatus.APPROVED);
        long base = 0L;
        for (UserChallenge uc : approved) {
            EnergyLevel level = uc.getChallenge() != null ? uc.getChallenge().getEnergyLevel() : null;
            base += eloForEnergy(level);
        }
        return base + (long) Math.max(currentStreak, 0) * ELO_STREAK_BONUS_PER_DAY;
    }

    private static long eloForEnergy(EnergyLevel level) {
        if (level == null) {
            return ELO_MEDIUM;
        }
        return switch (level) {
            case LOW -> ELO_LOW;
            case MEDIUM -> ELO_MEDIUM;
            case HIGH -> ELO_HIGH;
        };
    }

    /**
     * True if the user already has a confirmed fail for the given day — either
     * an EXPIRED quest or a COMPLETED quest that an admin REJECTED — and there
     * is no APPROVED quest on the same day to offset it. PENDING is treated as
     * undecided and doesn't break the streak yet.
     */
    private boolean hasFailedDay(Long userId, LocalDate day) {
        LocalDateTime startOfDay = day.atStartOfDay();
        List<UserChallenge> dayQuests = userChallengeRepository
                .findByUser_IdAndStartTimeAfter(userId, startOfDay);
        boolean anyFail = false;
        for (UserChallenge uc : dayQuests) {
            if (uc.getStartTime() == null || !uc.getStartTime().toLocalDate().equals(day)) {
                continue;
            }
            if (uc.getStatus() == CompletionStatus.COMPLETED
                    && uc.getModerationStatus() == ModerationStatus.APPROVED) {
                return false;
            }
            if (uc.getStatus() == CompletionStatus.EXPIRED
                    || (uc.getStatus() == CompletionStatus.COMPLETED
                            && uc.getModerationStatus() == ModerationStatus.REJECTED)) {
                anyFail = true;
            }
        }
        return anyFail;
    }

    /**
     * Flips this user's ASSIGNED quests whose deadline has passed to EXPIRED.
     * Called from {@link #evaluateAndAssignBadges} so any read of the profile
     * promotes the latest "you missed it" decisions before stats are computed.
     */
    private void sweepExpiredChallenges(Long userId) {
        List<UserChallenge> active = userChallengeRepository
                .findByUser_IdAndStatus(userId, CompletionStatus.ASSIGNED);
        LocalDateTime now = LocalDateTime.now();
        for (UserChallenge uc : active) {
            LocalDateTime deadline = uc.getEffectiveDeadline();
            if (deadline != null && now.isAfter(deadline)) {
                uc.setStatus(CompletionStatus.EXPIRED);
                userChallengeRepository.save(uc);
            }
        }
    }

    private int calculateCurrentStreak(Long userId) {
        // Immediate break: if today is a confirmed fail (EXPIRED or REJECTED
        // without an APPROVED) — the streak window for today is closed.
        LocalDate today = LocalDate.now();
        if (hasFailedDay(userId, today)) {
            return 0;
        }

        // Only APPROVED completions feed the streak chain — PENDING is in
        // flight and doesn't extend the chain until admin approves.
        List<LocalDate> completionDates = userChallengeRepository
                .findApprovedCompletionTimesDesc(userId).stream()
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .collect(Collectors.toList());
        if (completionDates.isEmpty()) {
            return 0;
        }

        LocalDate latestCompletionDate = completionDates.get(0);

        if (latestCompletionDate.isBefore(today.minusDays(1))) {
            return 0;
        }

        int streak = 0;
        LocalDate expectedDate = latestCompletionDate;

        for (LocalDate completionDate : completionDates) {
            if (completionDate.equals(expectedDate)) {
                streak++;
                expectedDate = expectedDate.minusDays(1);
            } else if (completionDate.isBefore(expectedDate)) {
                break;
            }
        }

        return streak;
    }

    private boolean doesUserMeetCriteria(String criteria, long completedChallenges, int currentStreak) {
        CriteriaRule rule = parseCriteria(criteria);
        if (rule == null) {
            return false;
        }

        return switch (rule.metric()) {
            case CRITERIA_STREAK_DAYS -> currentStreak >= rule.threshold();
            case CRITERIA_COMPLETED_CHALLENGES -> completedChallenges >= rule.threshold();
            default -> false;
        };
    }

    private CriteriaRule parseCriteria(String criteria) {
        if (criteria == null || criteria.trim().isEmpty()) {
            return null;
        }

        String[] parts = criteria.trim().split(":");
        if (parts.length != 2) {
            return null;
        }

        String metric = parts[0].trim().toUpperCase();
        int threshold;

        try {
            threshold = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException ex) {
            return null;
        }

        return new CriteriaRule(metric, threshold);
    }

    public List<String> getEarnedBadgeIds(User user) {
        if (user == null || user.getBadges() == null) {
            return List.of();
        }
        return user.getBadges().stream()
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
    }

    private record CriteriaRule(String metric, int threshold) {
    }
}
