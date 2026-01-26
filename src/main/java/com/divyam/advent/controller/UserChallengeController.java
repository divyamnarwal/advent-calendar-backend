package com.divyam.advent.controller;

import com.divyam.advent.dto.UserProgressDto;
import com.divyam.advent.enums.CompletionStatus;
import com.divyam.advent.model.UserChallenge;
import com.divyam.advent.service.UserChallengeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for UserChallenge operations.
 * Handles all HTTP requests related to user-challenge participation.
 */
@RestController
@RequestMapping("/user-challenges")
public class UserChallengeController {

    private final UserChallengeService userChallengeService;

    @Autowired
    public UserChallengeController(UserChallengeService userChallengeService) {
        this.userChallengeService = userChallengeService;
    }

    /**
     * Join a challenge - Register a user for a challenge.
     * POST /user-challenges/join?userId=1&challengeId=5
     */
    @PostMapping("/join")
    public ResponseEntity<UserChallenge> joinChallenge(
            @RequestParam Long userId,
            @RequestParam Long challengeId) {
        UserChallenge userChallenge = userChallengeService.joinChallenge(userId, challengeId);
        return new ResponseEntity<>(userChallenge, HttpStatus.CREATED);
    }

    /**
     * Get all challenges for a specific user.
     * GET /user-challenges/user/1
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserChallenge>> getUserChallenges(@PathVariable Long userId) {
        List<UserChallenge> userChallenges = userChallengeService.getUserChallenges(userId);
        return ResponseEntity.ok(userChallenges);
    }

    /**
     * Get all challenges for a user with a specific status.
     * GET /user-challenges/user/1/status?status=COMPLETED
     */
    @GetMapping("/user/{userId}/status")
    public ResponseEntity<List<UserChallenge>> getUserChallengesByStatus(
            @PathVariable Long userId,
            @RequestParam CompletionStatus status) {
        List<UserChallenge> userChallenges = userChallengeService.getUserChallengesByStatus(userId, status);
        return ResponseEntity.ok(userChallenges);
    }

    /**
     * Get progress statistics for a user.
     * Uses efficient COUNT queries to calculate totals at the database level.
     * GET /user-challenges/user/1/progress
     */
    @GetMapping("/user/{userId}/progress")
    public ResponseEntity<UserProgressDto> getUserProgress(@PathVariable Long userId) {
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
    public ResponseEntity<UserChallenge> getOrAssignDailyChallenge(@RequestParam Long userId) {
        UserChallenge dailyChallenge = userChallengeService.getOrAssignDailyChallenge(userId);
        return ResponseEntity.ok(dailyChallenge);
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
    public ResponseEntity<UserChallenge> getUserChallengeById(@PathVariable Long id) {
        UserChallenge userChallenge = userChallengeService.getUserChallengeById(id);
        return ResponseEntity.ok(userChallenge);
    }

    /**
     * Mark a challenge as completed.
     * PUT /user-challenges/1/complete
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<UserChallenge> markAsCompleted(@PathVariable Long id) {
        UserChallenge userChallenge = userChallengeService.markAsCompleted(id);
        return ResponseEntity.ok(userChallenge);
    }

    /**
     * Update the status of a UserChallenge.
     * PUT /user-challenges/1/status?status=IN_PROGRESS
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<UserChallenge> updateStatus(
            @PathVariable Long id,
            @RequestParam CompletionStatus status) {
        UserChallenge userChallenge = userChallengeService.updateStatus(id, status);
        return ResponseEntity.ok(userChallenge);
    }
}
