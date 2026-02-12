package com.divyam.advent.controller;

import com.divyam.advent.dto.MonthlyRecapResponseDto;
import com.divyam.advent.model.User;
import com.divyam.advent.service.AuthService;
import com.divyam.advent.service.RecapService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/recap")
public class RecapController {

    private final RecapService recapService;
    private final AuthService authService;

    public RecapController(RecapService recapService, AuthService authService) {
        this.recapService = recapService;
        this.authService = authService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<MonthlyRecapResponseDto> getMonthlyRecap(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String month
    ) {
        User currentUser = authService.getCurrentUser(jwt);
        YearMonth parsedMonth = null;
        if (month != null && !month.trim().isEmpty()) {
            parsedMonth = YearMonth.parse(month.trim());
        }

        MonthlyRecapResponseDto recap = recapService.getMonthlyRecap(currentUser.getId(), parsedMonth);
        return ResponseEntity.ok(recap);
    }
}
