package com.divyam.advent.service;

import com.divyam.advent.dto.MonthlyRecapResponseDto;
import com.divyam.advent.enums.ChallengeCategory;
import com.divyam.advent.enums.CompletionStatus;
import com.divyam.advent.exception.ResourceNotFoundException;
import com.divyam.advent.model.Challenge;
import com.divyam.advent.model.Photo;
import com.divyam.advent.model.User;
import com.divyam.advent.model.UserChallenge;
import com.divyam.advent.repository.PhotoRepository;
import com.divyam.advent.repository.TimeCapsuleRepository;
import com.divyam.advent.repository.UserChallengeRepository;
import com.divyam.advent.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecapServiceImplTest {

    @Mock
    private UserChallengeRepository userChallengeRepository;

    @Mock
    private TimeCapsuleRepository timeCapsuleRepository;

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private UserRepository userRepository;

    private RecapServiceImpl recapService;

    @BeforeEach
    void setUp() {
        recapService = new RecapServiceImpl(
                userChallengeRepository,
                timeCapsuleRepository,
                photoRepository,
                userRepository
        );
    }

    @Test
    void getMonthlyRecap_throwsWhenUserMissing() {
        when(userRepository.existsById(42L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> recapService.getMonthlyRecap(42L, YearMonth.of(2026, 2)));
    }

    @Test
    void getMonthlyRecap_returnsAggregatedStats() {
        Long userId = 5L;
        YearMonth month = YearMonth.of(2026, 2);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userChallengeRepository.countAssignedInRange(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(12L);
        when(userChallengeRepository.countCompletedInRange(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(8L);
        when(userChallengeRepository.countCompletedByCategoryInRange(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(categoryCount(ChallengeCategory.EXPLORE_CITY, 5L)));

        when(timeCapsuleRepository.countByUserIdAndCreatedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(3L);
        when(timeCapsuleRepository.countUnlockedInRange(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(2L);

        when(photoRepository.countByUserIdAndCreatedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(4L);
        when(photoRepository.findTop8ByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                eq(userId),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(photo(100L)));

        when(userChallengeRepository.findByUser_IdAndStatus(userId, CompletionStatus.COMPLETED))
                .thenReturn(List.of(
                        completedChallengeForDayOffset(-2),
                        completedChallengeForDayOffset(-1),
                        completedChallengeForDayOffset(0)
                ));

        MonthlyRecapResponseDto response = recapService.getMonthlyRecap(userId, month);

        assertEquals("2026-02", response.getMonth());
        assertEquals(12L, response.getTotalAssignedThisMonth());
        assertEquals(8L, response.getTotalCompletedThisMonth());
        assertEquals("EXPLORE_CITY", response.getTopCategory());
        assertEquals(5L, response.getTopCategoryCount());
        assertEquals(3L, response.getCapsulesCreatedThisMonth());
        assertEquals(2L, response.getCapsulesUnlockedThisMonth());
        assertEquals(4L, response.getPhotosAddedThisMonth());
        assertEquals(3, response.getCurrentStreakDays());
        assertEquals(3, response.getLongestStreakDays());
        assertEquals(1, response.getRecentPhotos().size());
    }

    private UserChallenge completedChallengeForDayOffset(int dayOffset) {
        User user = new User();
        user.setId(1L);
        Challenge challenge = new Challenge();
        challenge.setId(1L);
        challenge.setCategory(ChallengeCategory.EXPLORE_CITY);
        challenge.setActive(true);

        UserChallenge userChallenge = new UserChallenge(user, challenge, CompletionStatus.COMPLETED);
        LocalDateTime moment = LocalDate.now().plusDays(dayOffset).atStartOfDay().plusHours(10);
        userChallenge.setStartTime(moment);
        userChallenge.setCompletionTime(moment);
        return userChallenge;
    }

    private Photo photo(Long id) {
        Photo photo = new Photo();
        photo.setId(id);
        photo.setSecureUrl("https://example.com/test.jpg");
        photo.setCaption("Sample");
        photo.setCreatedAt(LocalDateTime.now());
        return photo;
    }

    private UserChallengeRepository.CategoryCountProjection categoryCount(ChallengeCategory category, long count) {
        return new UserChallengeRepository.CategoryCountProjection() {
            @Override
            public ChallengeCategory getCategory() {
                return category;
            }

            @Override
            public long getCount() {
                return count;
            }
        };
    }
}
