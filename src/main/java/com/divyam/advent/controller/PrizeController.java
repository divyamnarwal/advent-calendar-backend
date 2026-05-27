package com.divyam.advent.controller;

import com.divyam.advent.dto.PrizeDto;
import com.divyam.advent.model.User;
import com.divyam.advent.service.AuthService;
import com.divyam.advent.service.PrizeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * User-facing prizes: what's up for grabs this period, and what the current user has won.
 */
@RestController
@RequestMapping("/prizes")
public class PrizeController {

    private final PrizeService prizeService;
    private final AuthService authService;

    public PrizeController(PrizeService prizeService, AuthService authService) {
        this.prizeService = prizeService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<PrizeDto>> activePrizes() {
        return ResponseEntity.ok(prizeService.listActive());
    }

    @GetMapping("/me")
    public ResponseEntity<List<PrizeDto>> myPrizes(@AuthenticationPrincipal Jwt jwt) {
        User user = authService.getCurrentUser(jwt);
        return ResponseEntity.ok(prizeService.listAwardedToUser(user.getId()));
    }
}
