package com.divyam.advent.service;

import com.divyam.advent.dto.ProfileBadgeDto;
import com.divyam.advent.dto.ProfileBadgesResponseDto;
import com.divyam.advent.dto.ProfileResponseDto;
import com.divyam.advent.dto.ProfileUpdateRequestDto;
import com.divyam.advent.dto.ThemePreferenceUpdateRequestDto;
import com.divyam.advent.enums.ThemePreference;
import com.divyam.advent.exception.ResourceNotFoundException;
import com.divyam.advent.model.Badge;
import com.divyam.advent.model.User;
import com.divyam.advent.repository.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final BadgeService badgeService;

    public ProfileService(AuthService authService, UserRepository userRepository, BadgeService badgeService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.badgeService = badgeService;
    }

    @Transactional
    public ProfileResponseDto getProfile(Jwt jwt) {
        User user = getCurrentUserForProfileOps(jwt);
        List<Badge> newlyUnlocked = badgeService.evaluateAndAssignBadges(user);
        return toProfileResponse(user, newlyUnlocked);
    }

    @Transactional
    public ProfileResponseDto updateProfile(Jwt jwt, ProfileUpdateRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        User user = getCurrentUserForProfileOps(jwt);
        boolean hasUpdate = false;

        if (request.isNameProvided()) {
            if (request.getName() == null) {
                throw new IllegalArgumentException("Name cannot be blank");
            }
            String trimmedName = request.getName().trim();
            if (trimmedName.isEmpty()) {
                throw new IllegalArgumentException("Name cannot be blank");
            }
            user.setName(trimmedName);
            hasUpdate = true;
        }

        if (request.isAvatarProvided()) {
            if (request.getAvatar() == null) {
                user.setAvatar(null);
            } else {
                String trimmedAvatar = request.getAvatar().trim();
                user.setAvatar(trimmedAvatar.isEmpty() ? null : trimmedAvatar);
            }
            hasUpdate = true;
        }

        if (!hasUpdate) {
            throw new IllegalArgumentException("At least one profile field (name/avatar) must be provided");
        }

        userRepository.save(user);
        List<Badge> newlyUnlocked = badgeService.evaluateAndAssignBadges(user);
        return toProfileResponse(user, newlyUnlocked);
    }

    @Transactional
    public ProfileResponseDto updateThemePreference(Jwt jwt, ThemePreferenceUpdateRequestDto request) {
        if (request == null || request.getThemePreference() == null) {
            throw new IllegalArgumentException("themePreference is required");
        }

        User user = getCurrentUserForProfileOps(jwt);
        user.setThemePreference(request.getThemePreference());
        userRepository.save(user);

        List<Badge> newlyUnlocked = badgeService.evaluateAndAssignBadges(user);
        return toProfileResponse(user, newlyUnlocked);
    }

    @Transactional
    public ProfileBadgesResponseDto getProfileBadges(Jwt jwt) {
        User user = getCurrentUserForProfileOps(jwt);
        List<Badge> newlyUnlocked = badgeService.evaluateAndAssignBadges(user);

        List<Badge> allBadges = badgeService.getAllBadges();
        Set<String> earnedBadgeIds = new LinkedHashSet<>(badgeService.getEarnedBadgeIds(user));
        Set<String> newlyUnlockedIds = newlyUnlocked.stream()
                .map(Badge::getId)
                .collect(Collectors.toSet());

        List<ProfileBadgeDto> badgeDtos = allBadges.stream()
                .map(badge -> toProfileBadgeDto(
                        badge,
                        earnedBadgeIds.contains(badge.getId()),
                        newlyUnlockedIds.contains(badge.getId())
                ))
                .collect(Collectors.toList());

        List<ProfileBadgeDto> earnedBadges = badgeDtos.stream()
                .filter(ProfileBadgeDto::isEarned)
                .collect(Collectors.toList());

        return new ProfileBadgesResponseDto(
                badgeDtos,
                earnedBadges,
                newlyUnlockedIds.stream().sorted().collect(Collectors.toList())
        );
    }

    private ProfileResponseDto toProfileResponse(User user, List<Badge> newlyUnlocked) {
        ThemePreference themePreference = user.getThemePreference() != null
                ? user.getThemePreference()
                : ThemePreference.SYSTEM;

        return new ProfileResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAvatar(),
                user.getStreak() != null ? user.getStreak() : 0,
                user.getTotalPoints() != null ? user.getTotalPoints() : 0L,
                badgeService.getEarnedBadgeIds(user),
                themePreference,
                newlyUnlocked.stream().map(Badge::getId).sorted().collect(Collectors.toList())
        );
    }

    private ProfileBadgeDto toProfileBadgeDto(Badge badge, boolean earned, boolean newlyUnlocked) {
        return new ProfileBadgeDto(
                badge.getId(),
                badge.getTitle(),
                badge.getDescription(),
                badge.getIcon(),
                badge.getCriteria(),
                earned,
                newlyUnlocked
        );
    }

    private User getCurrentUserForProfileOps(Jwt jwt) {
        User currentUser = authService.getCurrentUser(jwt);
        return userRepository.findByIdForUpdate(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));
    }
}
