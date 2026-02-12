package com.divyam.advent.controller;

import com.divyam.advent.dto.DailyChallengeConfirmRequest;
import com.divyam.advent.dto.UserProgressDto;
import com.divyam.advent.enums.CompletionStatus;
import com.divyam.advent.enums.Mood;
import com.divyam.advent.model.UserChallenge;
import com.divyam.advent.service.AuthService;
import com.divyam.advent.service.UserChallengeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for UserChallenge operations.
 * Handles all HTTP requests related to user-challenge participation.
 */
@RestController
@RequestMapping("/user-challenges")
public class UserChallengeController {

    private final UserChallengeService userChallengeService;
    private final AuthService authService;

    @Autowired
    public UserChallengeController(UserChallengeService userChallengeService, AuthService authService) {
        this.userChallengeService = userChallengeService;
        this.authService = authService;
    }

    /**
     * Join a challenge - Register a user for a challenge.
     * POST /user-challenges/join?userId=1&challengeId=5
     */
    @PostMapping("/join")
    public ResponseEntity<UserChallenge> joinChallenge(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long userId,
            @RequestParam Long challengeId) {
        authService.validateUserAccess(jwt, userId);
        UserChallenge userChallenge = userChallengeService.joinChallenge(userId, challengeId);
        return new ResponseEntity<>(userChallenge, HttpStatus.CREATED);
    }

    /**
     * Get all challenges for a specific user.
     * GET /user-challenges/user/1
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserChallenge>> getUserChallenges(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long userId
    ) {
        authService.validateUserAccess(jwt, userId);
        List<UserChallenge> userChallenges = userChallengeService.getUserChallenges(userId);
        return ResponseEntity.ok(userChallenges);
    }

    /**
     * Get all challenges for a user with a specific status.
     * GET /user-challenges/user/1/status?status=COMPLETED
     */
    @GetMapping("/user/{userId}/status")
    public ResponseEntity<List<UserChallenge>> getUserChallengesByStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long userId,
            @RequestParam CompletionStatus status) {
        authService.validateUserAccess(jwt, userId);
        List<UserChallenge> userChallenges = userChallengeService.getUserChallengesByStatus(userId, status);
        return ResponseEntity.ok(userChallenges);
    }

    /**
     * Get progress statistics for a user.
     * Uses efficient COUNT queries to calculate totals at the database level.
     * GET /user-challenges/user/1/progress
     */
    @GetMapping("/user/{userId}/progress")
    public ResponseEntity<UserProgressDto> getUserProgress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long userId
    ) {
        authService.validateUserAccess(jwt, userId);
        UserProgressDto progress = userChallengeService.getUserProgress(userId);
        return ResponseEntity.ok(progress);
    }

    /**
     * Get or assign today's daily challenge for a user.
     * If the user already has a daily challenge assigned today, it will be returned.
     * Otherwise, a random active DAILY challenge will be assigned.
     * GET /user-challenges/daily?userId=1
     */
    @GetMapping("/daily")
    public ResponseEntity<UserChallenge> getOrAssignDailyChallenge(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long userId
    ) {
        authService.validateUserAccess(jwt, userId);
        UserChallenge dailyChallenge = userChallengeService.getOrAssignDailyChallenge(userId);
        return ResponseEntity.ok(dailyChallenge);
    }

    /**
     * Confirm today's daily challenge after preview.
     * POST /user-challenges/daily/confirm
     */
    @PostMapping("/daily/confirm")
    public ResponseEntity<UserChallenge> confirmDailyChallenge(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody DailyChallengeConfirmRequest request) {
        authService.validateUserAccess(jwt, request.getUserId());
        UserChallenge userChallenge = userChallengeService.confirmDailyChallenge(
                request.getUserId(),
                request.getChallengeId(),
                request.getMood()
        );
        return ResponseEntity.ok(userChallenge);
    }

    /**
     * Get all users participating in a specific challenge.
     * GET /user-challenges/challenge/5
     */
    @GetMapping("/challenge/{challengeId}")
    public ResponseEntity<List<UserChallenge>> getChallengeParticipants(@PathVariable Long challengeId) {
        List<UserChallenge> userChallenges = userChallengeService.getChallengeParticipants(challengeId);
        return ResponseEntity.ok(userChallenges);
    }

    /**
     * Get a specific UserChallenge by ID.
     * GET /user-challenges/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserChallenge> getUserChallengeById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        UserChallenge userChallenge = userChallengeService.getUserChallengeById(id);
        Long ownerId = userChallenge.getUser().getId();
        authService.validateUserAccess(jwt, ownerId);
        return ResponseEntity.ok(userChallenge);
    }

    /**
     * Mark a challenge as completed.
     * PUT /user-challenges/1/complete
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<UserChallenge> markAsCompleted(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        UserChallenge existing = userChallengeService.getUserChallengeById(id);
        if (existing.getUser() == null || existing.getUser().getId() == null) {
            throw new AccessDeniedException("Challenge owner not found");
        }
        authService.validateUserAccess(jwt, existing.getUser().getId());
        UserChallenge userChallenge = userChallengeService.markAsCompleted(id);
        return ResponseEntity.ok(userChallenge);
    }

    /**
     * Update the status of a UserChallenge.
     * PUT /user-challenges/1/status?status=IN_PROGRESS
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<UserChallenge> updateStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestParam CompletionStatus status) {
        UserChallenge existing = userChallengeService.getUserChallengeById(id);
        if (existing.getUser() == null || existing.getUser().getId() == null) {
            throw new AccessDeniedException("Challenge owner not found");
        }
        authService.validateUserAccess(jwt, existing.getUser().getId());
        UserChallenge userChallenge = userChallengeService.updateStatus(id, status);
        return ResponseEntity.ok(userChallenge);
    }

    /**
     * Clear all pending (ASSIGNED) challenges for a user.
     * This allows users to reset their challenge queue.
     * COMPLETED challenges are preserved.
     * DELETE /user-challenges/clear-pending?userId=1
     */
    @DeleteMapping("/clear-pending")
    public ResponseEntity<Map<String, Object>> clearPendingChallenges(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long userId) {
        authService.validateUserAccess(jwt, userId);
        long deletedCount = userChallengeService.clearPendingChallenges(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("deletedCount", deletedCount);
        response.put("message", deletedCount > 0
            ? "Successfully cleared " + deletedCount + " pending challenge(s)"
            : "No pending challenges to clear");
        return ResponseEntity.ok(response);
    }

    /**
     * Start a specific challenge when user explicitly clicks "Start Challenge".
     * Creates a UserChallenge with explicit startTime timestamp.
     * POST /user-challenges/start?userId=1&challengeId=5&mood=LOW
     *
     * @param userId the ID of the user
     * @param challengeId the ID of the challenge to start
     * @param mood the user's selected mood (LOW, NEUTRAL, HIGH)
     * @return the created or existing UserChallenge with startTime set
     */
    @PostMapping("/start")
    public ResponseEntity<UserChallenge> startChallenge(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long userId,
            @RequestParam Long challengeId,
            @RequestParam Mood mood) {
        authService.validateUserAccess(jwt, userId);
        UserChallenge userChallenge = userChallengeService.startChallenge(userId, challengeId, mood);
        return ResponseEntity.ok(userChallenge);
    }
}
