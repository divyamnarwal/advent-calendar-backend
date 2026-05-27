package com.divyam.advent.service;

import com.divyam.advent.dto.LeaderboardEntryDto;
import com.divyam.advent.dto.ProfileBadgeDto;
import com.divyam.advent.dto.ProfileBadgesResponseDto;
import com.divyam.advent.dto.ProfileResponseDto;
import com.divyam.advent.dto.ProfileUpdateRequestDto;
import com.divyam.advent.dto.PublicProfileDto;
import com.divyam.advent.dto.SocialLinksDto;
import com.divyam.advent.dto.ThemePreferenceUpdateRequestDto;
import com.divyam.advent.enums.CompletionStatus;
import com.divyam.advent.enums.ModerationStatus;
import com.divyam.advent.enums.ThemePreference;
import com.divyam.advent.exception.ResourceNotFoundException;
import com.divyam.advent.model.Badge;
import com.divyam.advent.model.User;
import com.divyam.advent.repository.UserChallengeRepository;
import com.divyam.advent.repository.UserRepository;
import com.divyam.advent.security.AdminGuard;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private static final int MAX_BIO_LENGTH = 300;
    private static final Set<String> ALLOWED_ACCENTS = Set.of(
            "violet", "fuchsia", "rose", "amber", "emerald", "sky", "indigo", "slate");

    private final AuthService authService;
    private final UserRepository userRepository;
    private final BadgeService badgeService;
    private final UserChallengeRepository userChallengeRepository;
    private final AdminGuard adminGuard;
    private final String cloudinaryAssetPrefix;

    public ProfileService(
            AuthService authService,
            UserRepository userRepository,
            BadgeService badgeService,
            UserChallengeRepository userChallengeRepository,
            AdminGuard adminGuard,
            @org.springframework.beans.factory.annotation.Value("${cloudinary.cloud-name:}") String cloudName
    ) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.badgeService = badgeService;
        this.userChallengeRepository = userChallengeRepository;
        this.adminGuard = adminGuard;
        // If cloud-name is blank (dev/tests without Cloudinary), validation falls
        // back to "must start with https://res.cloudinary.com/" — still blocks
        // arbitrary external hosts.
        this.cloudinaryAssetPrefix = cloudName == null || cloudName.isBlank()
                ? "https://res.cloudinary.com/"
                : "https://res.cloudinary.com/" + cloudName.trim() + "/";
    }

    private void requireCloudinaryUrl(String url, String field) {
        if (url == null || url.isBlank()) return;
        if (!url.startsWith(cloudinaryAssetPrefix)) {
            throw new IllegalArgumentException(field + " must be a Cloudinary URL");
        }
    }

    @Transactional(readOnly = true)
    public PublicProfileDto getPublicProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        EloLevels.LevelInfo level = EloLevels.infoFor(user.getElo());
        long approved = userChallengeRepository.countByUser_IdAndStatusAndModerationStatus(
                userId, CompletionStatus.COMPLETED, ModerationStatus.APPROVED);
        long rejected = userChallengeRepository.countByUser_IdAndStatusAndModerationStatus(
                userId, CompletionStatus.COMPLETED, ModerationStatus.REJECTED);
        long expired = userChallengeRepository.countByUser_IdAndStatus(userId, CompletionStatus.EXPIRED);
        long failed = rejected + expired;
        Double winRate = computeWinRate(approved, failed);
        Badge featured = resolveFeaturedBadge(user);
        Set<String> earnedIds = new LinkedHashSet<>(badgeService.getEarnedBadgeIds(user));
        List<ProfileBadgeDto> earnedBadges = badgeService.getAllBadges().stream()
                .filter(b -> earnedIds.contains(b.getId()))
                .map(b -> new ProfileBadgeDto(
                        b.getId(), b.getTitle(), b.getDescription(), b.getIcon(), b.getCriteria(),
                        true, false))
                .collect(Collectors.toList());
        boolean superAdmin = adminGuard.isSuperAdmin(user.getAuthSubject());
        boolean admin = superAdmin || user.isAdmin();
        return new PublicProfileDto(
                user.getId(),
                user.getName(),
                user.getCountry() != null ? user.getCountry().name() : null,
                user.getAvatar(),
                user.getBio(),
                user.getAccentColor(),
                user.getBannerUrl(),
                featured != null ? featured.getId() : null,
                featured != null ? featured.getTitle() : null,
                featured != null ? featured.getIcon() : null,
                socialsOf(user),
                user.getElo(),
                level.level(),
                level.currentLevelElo(),
                level.nextLevelElo(),
                user.getStreak() != null ? user.getStreak() : 0,
                approved,
                failed,
                winRate,
                earnedBadges,
                admin,
                superAdmin
        );
    }

    private static Double computeWinRate(long completed, long failed) {
        long total = completed + failed;
        if (total == 0) {
            return null;
        }
        return (double) completed / total;
    }

    /** The featured badge, only if it is still among the user's earned badges. */
    private Badge resolveFeaturedBadge(User user) {
        String id = user.getFeaturedBadgeId();
        if (id == null || id.isBlank() || !badgeService.getEarnedBadgeIds(user).contains(id)) {
            return null;
        }
        return badgeService.getAllBadges().stream()
                .filter(b -> id.equals(b.getId()))
                .findFirst()
                .orElse(null);
    }

    private static String emptyToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static SocialLinksDto socialsOf(User user) {
        return new SocialLinksDto(
                user.getSocialVk(),
                user.getSocialTelegram(),
                user.getSocialWhatsapp(),
                user.getSocialInstagram(),
                user.getSocialTwitter()
        );
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryDto> getLeaderboard(int limit) {
        int cap = limit <= 0 ? 50 : Math.min(limit, 200);
        List<User> top = userRepository
                .findAll(PageRequest.of(0, cap, Sort.by(Sort.Direction.DESC, "elo")))
                .getContent();
        List<LeaderboardEntryDto> board = new ArrayList<>();
        int rank = 1;
        for (User user : top) {
            boolean superAdmin = adminGuard.isSuperAdmin(user.getAuthSubject());
            boolean admin = superAdmin || user.isAdmin();
            board.add(new LeaderboardEntryDto(
                    rank++,
                    user.getId(),
                    user.getName(),
                    user.getAvatar(),
                    user.getCountry() != null ? user.getCountry().name() : null,
                    user.getElo(),
                    EloLevels.levelFor(user.getElo()),
                    admin,
                    superAdmin
            ));
        }
        return board;
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
                String avatarValue = trimmedAvatar.isEmpty() ? null : trimmedAvatar;
                requireCloudinaryUrl(avatarValue, "avatar");
                user.setAvatar(avatarValue);
            }
            hasUpdate = true;
        }
        if (request.isAvatarPublicIdProvided()) {
            user.setAvatarPublicId(emptyToNull(request.getAvatarPublicId()));
            hasUpdate = true;
        }

        if (request.isBannerUrlProvided()) {
            String banner = emptyToNull(request.getBannerUrl());
            requireCloudinaryUrl(banner, "bannerUrl");
            user.setBannerUrl(banner);
            hasUpdate = true;
        }
        if (request.isBannerPublicIdProvided()) {
            user.setBannerPublicId(emptyToNull(request.getBannerPublicId()));
            hasUpdate = true;
        }

        if (request.isBioProvided()) {
            String bio = emptyToNull(request.getBio());
            if (bio != null && bio.length() > MAX_BIO_LENGTH) {
                bio = bio.substring(0, MAX_BIO_LENGTH);
            }
            user.setBio(bio);
            hasUpdate = true;
        }

        if (request.isAccentColorProvided()) {
            String accent = emptyToNull(request.getAccentColor());
            if (accent != null) {
                accent = accent.trim().toLowerCase();
                if (!ALLOWED_ACCENTS.contains(accent)) {
                    throw new IllegalArgumentException("Unsupported accent color: " + accent);
                }
            }
            user.setAccentColor(accent);
            hasUpdate = true;
        }

        if (request.isFeaturedBadgeIdProvided()) {
            String badgeId = emptyToNull(request.getFeaturedBadgeId());
            if (badgeId != null && !badgeService.getEarnedBadgeIds(user).contains(badgeId)) {
                throw new IllegalArgumentException("Featured badge must be one you have earned");
            }
            user.setFeaturedBadgeId(badgeId);
            hasUpdate = true;
        }

        if (request.isCountryProvided()) {
            user.setCountry(com.divyam.advent.enums.Culture.fromValue(request.getCountry()));
            hasUpdate = true;
        }

        if (request.isSocialVkProvided()) {
            user.setSocialVk(emptyToNull(request.getSocialVk()));
            hasUpdate = true;
        }
        if (request.isSocialTelegramProvided()) {
            user.setSocialTelegram(emptyToNull(request.getSocialTelegram()));
            hasUpdate = true;
        }
        if (request.isSocialWhatsappProvided()) {
            user.setSocialWhatsapp(emptyToNull(request.getSocialWhatsapp()));
            hasUpdate = true;
        }
        if (request.isSocialInstagramProvided()) {
            user.setSocialInstagram(emptyToNull(request.getSocialInstagram()));
            hasUpdate = true;
        }
        if (request.isSocialTwitterProvided()) {
            user.setSocialTwitter(emptyToNull(request.getSocialTwitter()));
            hasUpdate = true;
        }

        if (!hasUpdate) {
            throw new IllegalArgumentException("At least one profile field must be provided");
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
        EloLevels.LevelInfo level = EloLevels.infoFor(user.getElo());
        Badge featured = resolveFeaturedBadge(user);

        ProfileResponseDto response = new ProfileResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAvatar(),
                user.getBio(),
                user.getAccentColor(),
                user.getBannerUrl(),
                featured != null ? featured.getId() : null,
                featured != null ? featured.getTitle() : null,
                featured != null ? featured.getIcon() : null,
                socialsOf(user),
                user.getStreak() != null ? user.getStreak() : 0,
                user.getTotalPoints() != null ? user.getTotalPoints() : 0L,
                user.getElo(),
                level.level(),
                level.currentLevelElo(),
                level.nextLevelElo(),
                badgeService.getEarnedBadgeIds(user),
                themePreference,
                newlyUnlocked.stream().map(Badge::getId).sorted().collect(Collectors.toList())
        );
        response.setCountry(user.getCountry() != null ? user.getCountry().name() : null);
        response.setAvatarPublicId(user.getAvatarPublicId());
        response.setBannerPublicId(user.getBannerPublicId());
        long approved = userChallengeRepository.countByUser_IdAndStatusAndModerationStatus(
                user.getId(), CompletionStatus.COMPLETED, ModerationStatus.APPROVED);
        long rejected = userChallengeRepository.countByUser_IdAndStatusAndModerationStatus(
                user.getId(), CompletionStatus.COMPLETED, ModerationStatus.REJECTED);
        long expired = userChallengeRepository.countByUser_IdAndStatus(user.getId(), CompletionStatus.EXPIRED);
        long failed = rejected + expired;
        response.setFailedCount(failed);
        response.setWinRate(computeWinRate(approved, failed));
        return response;
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
