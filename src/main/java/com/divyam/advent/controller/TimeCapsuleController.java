package com.divyam.advent.controller;

import com.divyam.advent.dto.TimeCapsuleRequestDto;
import com.divyam.advent.dto.TimeCapsuleResponseDto;
import com.divyam.advent.service.AuthService;
import com.divyam.advent.service.TimeCapsuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for TimeCapsule operations.
 * Allows users to create and retrieve time-locked messages.
 */
@RestController
@RequestMapping("/time-capsules")
public class TimeCapsuleController {

    private final TimeCapsuleService timeCapsuleService;
    private final AuthService authService;

    @Autowired
    public TimeCapsuleController(TimeCapsuleService timeCapsuleService, AuthService authService) {
        this.timeCapsuleService = timeCapsuleService;
        this.authService = authService;
    }

    /**
     * Create a new time capsule.
     * Users can specify either a reveal date or number of days.
     *
     * POST /time-capsules?userId=1
     *
     * Body example:
     * {
     *   "content": "Dear future me, I hope you finished that project!",
     *   "daysUntilReveal": 30
     * }
     *
     * OR
     *
     * {
     *   "content": "Dear future me, I hope you finished that project!",
     *   "revealDate": "2026-02-18T12:00:00"
     * }
     *
     * @param userId the ID of the user creating the capsule
     * @param request the capsule request
     * @return the created capsule
     */
    @PostMapping
    public ResponseEntity<TimeCapsuleResponseDto> createCapsule(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long userId,
            @RequestBody TimeCapsuleRequestDto request) {
        authService.validateUserAccess(jwt, userId);
        TimeCapsuleResponseDto capsule = timeCapsuleService.createCapsule(userId, request);
        return new ResponseEntity<>(capsule, HttpStatus.CREATED);
    }

    /**
     * Get all revealed (ready to open) capsules for a user.
     * Only returns capsules whose reveal date has passed.
     *
     * GET /time-capsules/revealed?userId=1
     *
     * @param userId the ID of the user
     * @return list of revealed capsules
     */
    @GetMapping("/revealed")
    public ResponseEntity<List<TimeCapsuleResponseDto>> getRevealedCapsules(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long userId) {
        authService.validateUserAccess(jwt, userId);
        List<TimeCapsuleResponseDto> capsules = timeCapsuleService.getRevealedCapsules(userId);
        return ResponseEntity.ok(capsules);
    }

    /**
     * Get all pending (not yet revealed) capsules for a user.
     * Returns capsules that are still locked (reveal date in future).
     * Content is hidden for pending capsules.
     *
     * GET /time-capsules/pending?userId=1
     *
     * @param userId the ID of the user
     * @return list of pending capsules
     */
    @GetMapping("/pending")
    public ResponseEntity<List<TimeCapsuleResponseDto>> getPendingCapsules(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long userId) {
        authService.validateUserAccess(jwt, userId);
        List<TimeCapsuleResponseDto> capsules = timeCapsuleService.getPendingCapsules(userId);
        return ResponseEntity.ok(capsules);
    }
}
