package com.divyam.advent.repository;

import com.divyam.advent.enums.ChallengeCategory;
import com.divyam.advent.enums.CompletionStatus;
import com.divyam.advent.enums.Mood;
import com.divyam.advent.model.UserChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for UserChallenge entity.
 * Spring Data JPA automatically provides the implementation.
 */
@Repository
public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {

    /**
     * Find all challenges for a specific user.
     * @param userId the ID of the user
     * @return list of UserChallenges for this user
     */
    List<UserChallenge> findByUser_Id(Long userId);

    /**
     * Find all users participating in a specific challenge.
     * @param challengeId the ID of the challenge
     * @return list of UserChallenges for this challenge
     */
    List<UserChallenge> findByChallenge_Id(Long challengeId);

    /**
     * Find all challenges for a user with a specific status.
     * Example: Get all COMPLETED challenges for a user.
     * @param userId the ID of the user
     * @param status the completion status to filter by
     * @return list of UserChallenges matching the criteria
     */
    List<UserChallenge> findByUser_IdAndStatus(Long userId, CompletionStatus status);

    /**
     * Check if a user is already participating in a specific challenge.
     * Useful to prevent duplicate sign-ups.
     * @param userId the ID of the user
     * @param challengeId the ID of the challenge
     * @return true if user is already doing this challenge, false otherwise
     */
    boolean existsByUser_IdAndChallenge_Id(Long userId, Long challengeId);

    /**
     * Count total challenges assigned to a user.
     * This is an efficient COUNT query that executes at the database level.
     * @param userId the ID of the user
     * @return total count of challenges for this user
     */
    long countByUser_Id(Long userId);

    /**
     * Count challenges for a user with a specific status.
     * This is an efficient COUNT query that executes at the database level.
     * @param userId the ID of the user
     * @param status the completion status to count
     * @return count of challenges matching the criteria
     */
    long countByUser_IdAndStatus(Long userId, CompletionStatus status);

    /**
     * Find daily challenges assigned to a user within a date range.
     * Uses startTime to determine which day the challenge was assigned.
     * This is used to check if a user already has a daily challenge for today.
     *
     * @param userId the ID of the user
     * @param category the challenge category (typically DAILY)
     * @param start start of date range (e.g., 2024-01-15 00:00:00)
     * @param end end of date range (e.g., 2024-01-15 23:59:59)
     * @return list of UserChallenges matching the criteria
     */
    @Query("SELECT uc FROM UserChallenge uc WHERE uc.user.id = :userId " +
           "AND uc.challenge.category = :category " +
           "AND uc.startTime BETWEEN :start AND :end")
    List<UserChallenge> findByUser_IdAndChallenge_CategoryAndStartTimeBetween(
            @Param("userId") Long userId,
            @Param("category") ChallengeCategory category,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * Find the most recently assigned challenge for a user before a given date.
     * Used to get yesterday's category and avoid repetition on consecutive days.
     *
     * @param userId the ID of the user
     * @param beforeDate date threshold (typically today's start)
     * @return the most recent UserChallenge before the given date, or null if none found
     */
    @Query("SELECT uc FROM UserChallenge uc WHERE uc.user.id = :userId " +
           "AND uc.startTime < :beforeDate " +
           "ORDER BY uc.startTime DESC")
    List<UserChallenge> findLatestChallengeBeforeDate(
            @Param("userId") Long userId,
            @Param("beforeDate") LocalDateTime beforeDate
    );

    /**
     * Find all challenges assigned to a user after a given date.
     * Used to check if user already has a challenge assigned today.
     *
     * @param userId the ID of the user
     * @param afterDate date threshold (typically today's start)
     * @return list of UserChallenges assigned after the given date
     */
    @Query("SELECT uc FROM UserChallenge uc WHERE uc.user.id = :userId " +
           "AND uc.startTime >= :afterDate")
    List<UserChallenge> findByUser_IdAndStartTimeAfter(
            @Param("userId") Long userId,
            @Param("afterDate") LocalDateTime afterDate
    );

    // ==================== ANALYTICS / PULSE QUERIES ====================
    // All queries below are for the Global Student Pulse feature.
    // They use efficient COUNT aggregation at the database level.

    /**
     * Count total unique users assigned challenges today.
     * Uses COUNT(DISTINCT user.id) for accurate user count.
     *
     * @param start start of today (00:00:00)
     * @param end end of today (23:59:59)
     * @return number of unique users assigned challenges today
     */
    @Query("SELECT COUNT(DISTINCT uc.user.id) FROM UserChallenge uc " +
           "WHERE uc.startTime BETWEEN :start AND :end")
    long countDistinctUsersToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Count completed challenges for today.
     *
     * @param start start of today (00:00:00)
     * @param end end of today (23:59:59)
     * @return number of completed challenges today
     */
    @Query("SELECT COUNT(uc) FROM UserChallenge uc " +
           "WHERE uc.status = 'COMPLETED' " +
           "AND uc.startTime BETWEEN :start AND :end")
    long countCompletedToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Count users with LOW mood today.
     *
     * @param start start of today (00:00:00)
     * @param end end of today (23:59:59)
     * @return number of users with LOW mood
     */
    @Query("SELECT COUNT(uc) FROM UserChallenge uc " +
           "WHERE uc.mood = 'LOW' " +
           "AND uc.startTime BETWEEN :start AND :end")
    long countLowMoodToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Count users with NEUTRAL mood today.
     *
     * @param start start of today (00:00:00)
     * @param end end of today (23:59:59)
     * @return number of users with NEUTRAL mood
     */
    @Query("SELECT COUNT(uc) FROM UserChallenge uc " +
           "WHERE uc.mood = 'NEUTRAL' " +
           "AND uc.startTime BETWEEN :start AND :end")
    long countNeutralMoodToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Count users with HIGH mood today.
     *
     * @param start start of today (00:00:00)
     * @param end end of today (23:59:59)
     * @return number of users with HIGH mood
     */
    @Query("SELECT COUNT(uc) FROM UserChallenge uc " +
           "WHERE uc.mood = 'HIGH' " +
           "AND uc.startTime BETWEEN :start AND :end")
    long countHighMoodToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
