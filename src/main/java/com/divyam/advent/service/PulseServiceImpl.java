package com.divyam.advent.service;

import com.divyam.advent.dto.PulseResponseDto;
import com.divyam.advent.repository.UserChallengeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of PulseService.
 * Contains the business logic for computing daily anonymized analytics.
 */
@Service
public class PulseServiceImpl implements PulseService {

    private final UserChallengeRepository userChallengeRepository;

    @Autowired
    public PulseServiceImpl(UserChallengeRepository userChallengeRepository) {
        this.userChallengeRepository = userChallengeRepository;
    }

    @Override
    public PulseResponseDto getTodayPulse() {
        // 1. Define today's date range
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();        // 00:00:00
        LocalDateTime endOfToday = today.atTime(23, 59, 59);      // 23:59:59

        // 2. Query all statistics using efficient COUNT queries
        long totalUsers = userChallengeRepository.countDistinctUsersToday(startOfToday, endOfToday);
        long completedCount = userChallengeRepository.countCompletedToday(startOfToday, endOfToday);
        long lowMoodCount = userChallengeRepository.countLowMoodToday(startOfToday, endOfToday);
        long neutralMoodCount = userChallengeRepository.countNeutralMoodToday(startOfToday, endOfToday);
        long highMoodCount = userChallengeRepository.countHighMoodToday(startOfToday, endOfToday);

        // 3. Handle edge case: no data today
        if (totalUsers == 0) {
            return new PulseResponseDto();
        }

        // 4. Build and return the response
        String dateString = today.format(DateTimeFormatter.ISO_LOCAL_DATE); // YYYY-MM-DD
        return new PulseResponseDto(
                dateString,
                totalUsers,
                completedCount,
                lowMoodCount,
                neutralMoodCount,
                highMoodCount
        );
    }
}
