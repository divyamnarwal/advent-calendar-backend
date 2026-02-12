package com.divyam.advent.service;

import com.divyam.advent.enums.CompletionStatus;
import com.divyam.advent.enums.ThemePreference;
import com.divyam.advent.model.Badge;
import com.divyam.advent.model.User;
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

        long completedChallenges = userChallengeRepository.countByUser_IdAndStatus(
                user.getId(),
                CompletionStatus.COMPLETED
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

    private int calculateCurrentStreak(Long userId) {
        List<LocalDate> completionDates = userChallengeRepository.findCompletionTimesDesc(userId).stream()
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .collect(Collectors.toList());
        if (completionDates.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
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
