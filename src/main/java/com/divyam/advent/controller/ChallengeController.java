
package com.divyam.advent.controller;

import com.divyam.advent.enums.ChallengeCategory;
import com.divyam.advent.enums.Mood;
import com.divyam.advent.model.Challenge;
import com.divyam.advent.model.UserChallenge;
import com.divyam.advent.service.AuthService;
import com.divyam.advent.service.ChallengeService;
import com.divyam.advent.service.UserChallengeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;
    private final UserChallengeService userChallengeService;
    private final AuthService authService;

    public ChallengeController(
            ChallengeService challengeService,
            UserChallengeService userChallengeService,
            AuthService authService
    ) {
        this.challengeService = challengeService;
        this.userChallengeService = userChallengeService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<Challenge> createChallenge(@RequestBody Challenge challenge) {
        Challenge createdChallenge = challengeService.createChallenge(challenge);
        return new ResponseEntity<>(createdChallenge, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Challenge>> getAllChallenges() {
        List<Challenge> challenges = challengeService.getAllChallenges();
        return new ResponseEntity<>(challenges, HttpStatus.OK);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Challenge>> getActiveChallengesByCategory(@PathVariable ChallengeCategory category) {
        List<Challenge> challenges = challengeService.getActiveChallengesByCategory(category);
        return new ResponseEntity<>(challenges, HttpStatus.OK);
    }

    /**
     * Get today's challenge for a user based on their mood.
     *
     * Mood influences challenge selection:
     * - LOW mood → LOW energy challenges (easy, achievable)
     * - NEUTRAL mood → MEDIUM energy challenges (balanced)
     * - HIGH mood → HIGH energy challenges (exciting, ambitious)
     *
     * If user already has a DAILY challenge assigned today, the mood is updated
     * and the existing challenge is returned.
     *
     * GET /challenges/today?userId=1&mood=LOW
     *
     * @param userId the ID of the user
     * @param mood the user's current mood
     * @return the UserChallenge containing today's daily challenge
     */
    @GetMapping("/today")
    public ResponseEntity<UserChallenge> getTodaysChallenge(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long userId,
            @RequestParam Mood mood) {
        authService.validateUserAccess(jwt, userId);
        UserChallenge userChallenge = userChallengeService.getOrAssignDailyChallenge(userId, mood);
        return ResponseEntity.ok(userChallenge);
    }

    /**
     * Preview today's challenge without assigning it.
     * GET /challenges/today/preview?userId=1&mood=LOW
     */
    @GetMapping("/today/preview")
    public ResponseEntity<Challenge> previewTodaysChallenge(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long userId,
            @RequestParam Mood mood) {
        authService.validateUserAccess(jwt, userId);
        Challenge challenge = userChallengeService.previewDailyChallenge(userId, mood);
        return ResponseEntity.ok(challenge);
    }
}
