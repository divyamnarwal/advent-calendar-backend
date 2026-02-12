package com.divyam.advent.service;

import com.divyam.advent.dto.UserProgressDto;
import com.divyam.advent.enums.CompletionStatus;
import com.divyam.advent.enums.Mood;
import com.divyam.advent.model.Challenge;
import com.divyam.advent.model.UserChallenge;

import java.util.List;

/**
 * Service interface for UserChallenge operations.
 * This defines the business logic methods for managing user-challenge participation.
 */
public interface UserChallengeService {

    /**
     * Register a user for a challenge.
     * @param userId the ID of the user
     * @param challengeId the ID of the challenge
     * @return the created UserChallenge
     */
    UserChallenge joinChallenge(Long userId, Long challengeId);

    /**
     * Get all challenges for a specific user.
     * @param userId the ID of the user
     * @return list of UserChallenges
     */
    List<UserChallenge> getUserChallenges(Long userId);

    /**
     * Get all users participating in a specific challenge.
     * @param challengeId the ID of the challenge
     * @return list of UserChallenges
     */
    List<UserChallenge> getChallengeParticipants(Long challengeId);

    /**
     * Get a specific UserChallenge by ID.
     * @param id the UserChallenge ID
     * @return the UserChallenge
     */
    UserChallenge getUserChallengeById(Long id);

    /**
     * Mark a challenge as completed for a user.
     * @param id the UserChallenge ID
     * @return the updated UserChallenge
     */
    UserChallenge markAsCompleted(Long id);

    /**
     * Update the status of a UserChallenge.
     * @param id the UserChallenge ID
     * @param status the new status
     * @return the updated UserChallenge
     */
    UserChallenge updateStatus(Long id, CompletionStatus status);

    /**
     * Get all challenges for a user with a specific status.
     * @param userId the ID of the user
     * @param status the completion status
     * @return list of UserChallenges
     */
    List<UserChallenge> getUserChallengesByStatus(Long userId, CompletionStatus status);

    /**
     * Get progress statistics for a user.
     * Uses efficient COUNT queries to calculate totals at the database level.
     * @param userId the ID of the user
     * @return UserProgressDto containing progress information
     */
    UserProgressDto getUserProgress(Long userId);

    /**
     * Get or assign today's daily challenge for a user.
     * - If user already has a daily challenge today, return it
     * - If not, randomly select one active DAILY challenge and assign it
     * This ensures only one daily challenge per user per day.
     * @param userId the ID of the user
     * @return the UserChallenge for today's daily challenge
     */
    UserChallenge getOrAssignDailyChallenge(Long userId);

    /**
     * Get or assign today's daily challenge for a user based on their mood.
     * Mood influences challenge selection by matching to energy level:
     * - LOW mood → LOW energy challenges (easy, achievable)
     * - NEUTRAL mood → MEDIUM energy challenges (balanced)
     * - HIGH mood → HIGH energy challenges (exciting, ambitious)
     *
     * If user already has a daily challenge today, mood is updated and returned.
     * @param userId the ID of the user
     * @param mood the user's current mood
     * @return the UserChallenge for today's daily challenge
     */
    UserChallenge getOrAssignDailyChallenge(Long userId, Mood mood);

    /**
     * Preview today's daily challenge for a user without assigning it.
     * @param userId the ID of the user
     * @param mood the user's current mood
     * @return the Challenge preview for today
     */
    Challenge previewDailyChallenge(Long userId, Mood mood);

    /**
     * Confirm today's daily challenge assignment after preview.
     * @param userId the ID of the user
     * @param challengeId the ID of the challenge to confirm
     * @param mood the user's current mood
     * @return the assigned UserChallenge
     */
    UserChallenge confirmDailyChallenge(Long userId, Long challengeId, Mood mood);

    /**
     * Clear all pending (ASSIGNED status) challenges for a user.
     * This allows users to reset their challenge queue and start fresh.
     * Only challenges with ASSIGNED status are deleted; COMPLETED challenges are preserved.
     *
     * @param userId the ID of the user
     * @return the number of pending challenges cleared
     */
    long clearPendingChallenges(Long userId);

    /**
     * Start a specific challenge when user explicitly clicks "Start Challenge".
     * Creates a UserChallenge with explicit startTime timestamp.
     * If user already has this challenge assigned for today, returns existing (idempotent).
     *
     * @param userId the ID of the user
     * @param challengeId the ID of the challenge to start
     * @param mood the user's selected mood (LOW, NEUTRAL, HIGH)
     * @return the created or existing UserChallenge
     */
    UserChallenge startChallenge(Long userId, Long challengeId, Mood mood);
}
