package com.divyam.advent.service;

import com.divyam.advent.dto.MonthlyRecapResponseDto;
import com.divyam.advent.dto.RecapPhotoPreviewDto;
import com.divyam.advent.enums.CompletionStatus;
import com.divyam.advent.exception.ResourceNotFoundException;
import com.divyam.advent.model.Photo;
import com.divyam.advent.model.UserChallenge;
import com.divyam.advent.repository.PhotoRepository;
import com.divyam.advent.repository.TimeCapsuleRepository;
import com.divyam.advent.repository.UserChallengeRepository;
import com.divyam.advent.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecapServiceImpl implements RecapService {

    private final UserChallengeRepository userChallengeRepository;
    private final TimeCapsuleRepository timeCapsuleRepository;
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;

    public RecapServiceImpl(
            UserChallengeRepository userChallengeRepository,
            TimeCapsuleRepository timeCapsuleRepository,
            PhotoRepository photoRepository,
            UserRepository userRepository
    ) {
        this.userChallengeRepository = userChallengeRepository;
        this.timeCapsuleRepository = timeCapsuleRepository;
        this.photoRepository = photoRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyRecapResponseDto getMonthlyRecap(Long userId, YearMonth month) {
        validateUser(userId);

        YearMonth targetMonth = month != null ? month : YearMonth.now();
        LocalDateTime monthStart = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = targetMonth.plusMonths(1).atDay(1).atStartOfDay().minusNanos(1);
        LocalDateTime now = LocalDateTime.now();

        long totalAssigned = userChallengeRepository.countAssignedInRange(userId, monthStart, monthEnd);
        long totalCompleted = userChallengeRepository.countCompletedInRange(userId, monthStart, monthEnd);

        List<UserChallengeRepository.CategoryCountProjection> categoryCounts =
                userChallengeRepository.countCompletedByCategoryInRange(userId, monthStart, monthEnd);

        String topCategory = null;
        long topCategoryCount = 0L;
        if (!categoryCounts.isEmpty()) {
            UserChallengeRepository.CategoryCountProjection top = categoryCounts.stream()
                    .sorted(
                            Comparator.comparingLong(UserChallengeRepository.CategoryCountProjection::getCount)
                                    .reversed()
                                    .thenComparing(p -> p.getCategory().name())
                    )
                    .findFirst()
                    .orElse(null);
            if (top != null) {
                topCategory = top.getCategory().name();
                topCategoryCount = top.getCount();
            }
        }

        long capsulesCreated = timeCapsuleRepository.countByUserIdAndCreatedAtBetween(userId, monthStart, monthEnd);
        long capsulesUnlocked = timeCapsuleRepository.countUnlockedInRange(userId, monthStart, monthEnd, now);
        long photosAdded = photoRepository.countByUserIdAndCreatedAtBetween(userId, monthStart, monthEnd);
        List<RecapPhotoPreviewDto> recentPhotos = photoRepository
                .findTop8ByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, monthStart, monthEnd)
                .stream()
                .map(this::toPreview)
                .collect(Collectors.toList());

        StreakStats streakStats = calculateStreakStats(userId);

        return new MonthlyRecapResponseDto(
                targetMonth.toString(),
                monthStart,
                monthEnd,
                totalAssigned,
                totalCompleted,
                streakStats.currentStreak(),
                streakStats.longestStreak(),
                topCategory,
                topCategoryCount,
                capsulesCreated,
                capsulesUnlocked,
                photosAdded,
                recentPhotos,
                now
        );
    }

    private void validateUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    private RecapPhotoPreviewDto toPreview(Photo photo) {
        return new RecapPhotoPreviewDto(
                photo.getId(),
                photo.getSecureUrl(),
                photo.getCaption(),
                photo.getCreatedAt()
        );
    }

    private StreakStats calculateStreakStats(Long userId) {
        List<UserChallenge> completedChallenges = userChallengeRepository.findByUser_IdAndStatus(
                userId,
                CompletionStatus.COMPLETED
        );

        Set<LocalDate> completedDays = completedChallenges.stream()
                .map(challenge -> {
                    LocalDateTime moment = challenge.getCompletionTime() != null
                            ? challenge.getCompletionTime()
                            : challenge.getStartTime();
                    return moment != null ? moment.toLocalDate() : null;
                })
                .filter(day -> day != null)
                .collect(Collectors.toSet());

        if (completedDays.isEmpty()) {
            return new StreakStats(0, 0);
        }

        List<LocalDate> sortedDays = completedDays.stream()
                .sorted()
                .collect(Collectors.toList());

        int longestStreak = 1;
        int runningLongest = 1;
        for (int i = 1; i < sortedDays.size(); i++) {
            long gap = ChronoUnit.DAYS.between(sortedDays.get(i - 1), sortedDays.get(i));
            if (gap == 1) {
                runningLongest += 1;
            } else {
                runningLongest = 1;
            }
            if (runningLongest > longestStreak) {
                longestStreak = runningLongest;
            }
        }

        LocalDate today = LocalDate.now();
        LocalDate latestCompletedDay = sortedDays.get(sortedDays.size() - 1);
        long gapFromToday = ChronoUnit.DAYS.between(latestCompletedDay, today);

        if (gapFromToday > 1 || gapFromToday < 0) {
            return new StreakStats(0, longestStreak);
        }

        int currentStreak = 1;
        for (int i = sortedDays.size() - 1; i > 0; i--) {
            long gap = ChronoUnit.DAYS.between(sortedDays.get(i - 1), sortedDays.get(i));
            if (gap == 1) {
                currentStreak += 1;
            } else {
                break;
            }
        }

        return new StreakStats(currentStreak, longestStreak);
    }

    private record StreakStats(int currentStreak, int longestStreak) {
    }
}
