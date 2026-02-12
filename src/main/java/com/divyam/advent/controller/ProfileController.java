package com.divyam.advent.controller;

import com.divyam.advent.dto.ProfileBadgesResponseDto;
import com.divyam.advent.dto.ProfileResponseDto;
import com.divyam.advent.dto.ProfileUpdateRequestDto;
import com.divyam.advent.dto.ThemePreferenceUpdateRequestDto;
import com.divyam.advent.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponseDto> getProfile(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(profileService.getProfile(jwt));
    }

    @PutMapping
    public ResponseEntity<ProfileResponseDto> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ProfileUpdateRequestDto request
    ) {
        return ResponseEntity.ok(profileService.updateProfile(jwt, request));
    }

    @PutMapping("/theme")
    public ResponseEntity<ProfileResponseDto> updateThemePreference(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ThemePreferenceUpdateRequestDto request
    ) {
        return ResponseEntity.ok(profileService.updateThemePreference(jwt, request));
    }

    @GetMapping("/badges")
    public ResponseEntity<ProfileBadgesResponseDto> getProfileBadges(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(profileService.getProfileBadges(jwt));
    }
}
