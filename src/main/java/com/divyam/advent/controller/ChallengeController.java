
package com.divyam.advent.controller;

import com.divyam.advent.enums.ChallengeCategory;
import com.divyam.advent.enums.Mood;
import com.divyam.advent.model.Challenge;
import com.divyam.advent.model.UserChallenge;
import com.divyam.advent.service.ChallengeService;
import com.divyam.advent.service.UserChallengeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;
    private final UserChallengeService userChallengeService;

    public ChallengeController(ChallengeService challengeService, UserChallengeService userChallengeService) {
        this.challengeService = challengeService;
        this.userChallengeService = userChallengeService;
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
            @RequestParam Long userId,
            @RequestParam Mood mood) {
        UserChallenge userChallenge = userChallengeService.getOrAssignDailyChallenge(userId, mood);
        return ResponseEntity.ok(userChallenge);
    }
}
