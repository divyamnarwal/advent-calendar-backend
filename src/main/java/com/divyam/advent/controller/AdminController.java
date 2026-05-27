package com.divyam.advent.controller;

import com.divyam.advent.dto.AdminUserChallengeDto;
import com.divyam.advent.dto.AdminUserSummaryDto;
import com.divyam.advent.dto.BanRequestDto;
import com.divyam.advent.dto.PrizeDto;
import com.divyam.advent.dto.PrizeLeaderboardEntryDto;
import com.divyam.advent.dto.PrizeRequestDto;
import com.divyam.advent.dto.ProfileContentDto;
import com.divyam.advent.enums.CompletionStatus;
import com.divyam.advent.enums.ModerationStatus;
import com.divyam.advent.exception.ResourceNotFoundException;
import com.divyam.advent.model.User;
import com.divyam.advent.model.UserChallenge;
import com.divyam.advent.repository.UserChallengeRepository;
import com.divyam.advent.security.AdminGuard;
import com.divyam.advent.service.BadgeService;
import com.divyam.advent.service.CloudinaryCleanupService;
import com.divyam.advent.service.PrizeService;
import com.divyam.advent.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminGuard adminGuard;
    private final UserService userService;
    private final UserChallengeRepository userChallengeRepository;
    private final CloudinaryCleanupService cloudinaryCleanupService;
    private final PrizeService prizeService;
    private final BadgeService badgeService;
    private final com.divyam.advent.service.AuditService auditService;
    private final com.divyam.advent.repository.AdminAuditLogRepository auditLogRepository;

    public AdminController(
            AdminGuard adminGuard,
            UserService userService,
            UserChallengeRepository userChallengeRepository,
            CloudinaryCleanupService cloudinaryCleanupService,
            PrizeService prizeService,
            BadgeService badgeService,
            com.divyam.advent.service.AuditService auditService,
            com.divyam.advent.repository.AdminAuditLogRepository auditLogRepository
    ) {
        this.adminGuard = adminGuard;
        this.userService = userService;
        this.userChallengeRepository = userChallengeRepository;
        this.cloudinaryCleanupService = cloudinaryCleanupService;
        this.prizeService = prizeService;
        this.badgeService = badgeService;
        this.auditService = auditService;
        this.auditLogRepository = auditLogRepository;
    }

    /** Look up the admin User performing the current request. Used for audit attribution. */
    private User actor(Jwt jwt) {
        return userService.getByAuthSubject("CLERK", getClerkUserId(jwt)).orElse(null);
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserSummaryDto>> listUsers(@AuthenticationPrincipal Jwt jwt) {
        adminGuard.requireAdmin(getClerkUserId(jwt));
        List<AdminUserSummaryDto> users = userService.getAllUsers().stream()
                .map(this::toSummary)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users/{id}/admin")
    public ResponseEntity<AdminUserSummaryDto> grantAdmin(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        adminGuard.requireSuperAdmin(getClerkUserId(jwt));
        User updated = userService.setAdminRole(id, true);
        auditService.record(actor(jwt), com.divyam.advent.service.AuditService.ACTION_ADMIN_GRANTED,
                com.divyam.advent.service.AuditService.TARGET_USER, id, null);
        return ResponseEntity.ok(toSummary(updated));
    }

    @DeleteMapping("/users/{id}/admin")
    public ResponseEntity<AdminUserSummaryDto> revokeAdmin(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        adminGuard.requireSuperAdmin(getClerkUserId(jwt));
        User updated = userService.setAdminRole(id, false);
        auditService.record(actor(jwt), com.divyam.advent.service.AuditService.ACTION_ADMIN_REVOKED,
                com.divyam.advent.service.AuditService.TARGET_USER, id, null);
        return ResponseEntity.ok(toSummary(updated));
    }

    // --- Bans ---------------------------------------------------------------

    @PostMapping("/users/{id}/ban")
    public ResponseEntity<AdminUserSummaryDto> banUser(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestBody(required = false) BanRequestDto request
    ) {
        adminGuard.requireAdmin(getClerkUserId(jwt));
        String reason = request != null ? request.getReason() : null;
        Integer days = request != null ? request.getDurationDays() : null;
        java.time.LocalDateTime expiresAt = days != null && days > 0
                ? java.time.LocalDateTime.now().plusDays(days)
                : null;
        AdminUserSummaryDto dto = toSummary(userService.setBan(id, reason, expiresAt));
        auditService.record(actor(jwt), com.divyam.advent.service.AuditService.ACTION_USER_BANNED,
                com.divyam.advent.service.AuditService.TARGET_USER, id,
                reason != null ? reason : (days != null ? days + " days" : "permanent"));
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/users/{id}/ban")
    public ResponseEntity<AdminUserSummaryDto> unbanUser(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        adminGuard.requireAdmin(getClerkUserId(jwt));
        AdminUserSummaryDto dto = toSummary(userService.clearBan(id));
        auditService.record(actor(jwt), com.divyam.advent.service.AuditService.ACTION_USER_UNBANNED,
                com.divyam.advent.service.AuditService.TARGET_USER, id, null);
        return ResponseEntity.ok(dto);
    }

    // --- Profile content (avatars + banners) --------------------------------

    @GetMapping("/profile-content")
    public ResponseEntity<List<ProfileContentDto>> profileContentFeed(@AuthenticationPrincipal Jwt jwt) {
        adminGuard.requireAdmin(getClerkUserId(jwt));
        List<ProfileContentDto> entries = userService.getAllUsers().stream()
                .filter(u -> (u.getAvatar() != null && !u.getAvatar().isBlank())
                        || (u.getBannerUrl() != null && !u.getBannerUrl().isBlank()))
                .map(u -> new ProfileContentDto(
                        u.getId(), u.getName(), u.getEmail(), u.getAvatar(), u.getBannerUrl()))
                .toList();
        return ResponseEntity.ok(entries);
    }

    @PostMapping("/users/{id}/clear-avatar")
    public ResponseEntity<AdminUserSummaryDto> clearUserAvatar(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        adminGuard.requireAdmin(getClerkUserId(jwt));
        User u = userService.getUserById(id);
        if (u.getAvatarPublicId() != null && !u.getAvatarPublicId().isBlank()) {
            cloudinaryCleanupService.destroyOnCloudinary(u.getAvatarPublicId());
        }
        u.setAvatar(null);
        u.setAvatarPublicId(null);
        AdminUserSummaryDto dto = toSummary(userService.createUser(u));
        auditService.record(actor(jwt), com.divyam.advent.service.AuditService.ACTION_AVATAR_CLEARED,
                com.divyam.advent.service.AuditService.TARGET_USER, id, null);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/users/{id}/clear-banner")
    public ResponseEntity<AdminUserSummaryDto> clearUserBanner(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        adminGuard.requireAdmin(getClerkUserId(jwt));
        User u = userService.getUserById(id);
        if (u.getBannerPublicId() != null && !u.getBannerPublicId().isBlank()) {
            cloudinaryCleanupService.destroyOnCloudinary(u.getBannerPublicId());
        }
        u.setBannerUrl(null);
        u.setBannerPublicId(null);
        AdminUserSummaryDto dto = toSummary(userService.createUser(u));
        auditService.record(actor(jwt), com.divyam.advent.service.AuditService.ACTION_BANNER_CLEARED,
                com.divyam.advent.service.AuditService.TARGET_USER, id, null);
        return ResponseEntity.ok(dto);
    }

    // --- Prizes -------------------------------------------------------------

    @GetMapping("/prizes")
    public ResponseEntity<List<PrizeDto>> listPrizes(@AuthenticationPrincipal Jwt jwt) {
        adminGuard.requireAdmin(getClerkUserId(jwt));
        return ResponseEntity.ok(prizeService.listAll());
    }

    @PostMapping("/prizes")
    public ResponseEntity<PrizeDto> createPrize(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody PrizeRequestDto request
    ) {
        adminGuard.requireAdmin(getClerkUserId(jwt));
        return ResponseEntity.ok(prizeService.create(request));
    }

    @PutMapping("/prizes/{id}")
    public ResponseEntity<PrizeDto> updatePrize(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestBody PrizeRequestDto request
    ) {
        adminGuard.requireAdmin(getClerkUserId(jwt));
        return ResponseEntity.ok(prizeService.update(id, request));
    }

    @DeleteMapping("/prizes/{id}")
    public ResponseEntity<Void> deletePrize(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        adminGuard.requireAdmin(getClerkUserId(jwt));
        prizeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/prizes/{id}/leaderboard")
    public ResponseEntity<List<PrizeLeaderboardEntryDto>> prizeLeaderboard(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestParam(defaultValue = "20") int limit
    ) {
        adminGuard.requireAdmin(getClerkUserId(jwt));
        return ResponseEntity.ok(prizeService.leaderboard(id, limit));
    }

    @PostMapping("/prizes/{id}/award")
    public ResponseEntity<PrizeDto> awardPrize(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        adminGuard.requireAdmin(getClerkUserId(jwt));
        return ResponseEntity.ok(prizeService.award(id, userId));
    }

    @DeleteMapping("/prizes/{id}/award")
    public ResponseEntity<PrizeDto> unawardPrize(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        adminGuard.requireAdmin(getClerkUserId(jwt));
        return ResponseEntity.ok(prizeService.unaward(id));
    }

    @GetMapping("/user-challenges")
    public ResponseEntity<List<AdminUserChallengeDto>> moderationFeed(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) ModerationStatus moderationStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        adminGuard.requireAdmin(getClerkUserId(jwt));
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200));
        List<UserChallenge> rows = moderationStatus == null
                ? userChallengeRepository.findByStatusOrderByCompletionTimeDesc(CompletionStatus.COMPLETED, pageable)
                : userChallengeRepository.findByStatusAndModerationStatusOrderByCompletionTimeDesc(
                        CompletionStatus.COMPLETED, moderationStatus, pageable);
        return ResponseEntity.ok(rows.stream().map(this::toDto).toList());
    }

    @PostMapping("/user-challenges/{id}/moderate")
    public ResponseEntity<AdminUserChallengeDto> moderate(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestParam ModerationStatus status
    ) {
        adminGuard.requireAdmin(getClerkUserId(jwt));
        UserChallenge uc = userChallengeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserChallenge not found with id: " + id));
        uc.setModerationStatus(status);
        UserChallenge saved = userChallengeRepository.save(uc);
        // Approval/rejection flips stats — re-evaluate so streak, win rate and
        // badges reflect the decision right away.
        if (saved.getUser() != null) {
            badgeService.evaluateAndAssignBadges(saved.getUser());
        }
        String action = switch (status) {
            case APPROVED -> com.divyam.advent.service.AuditService.ACTION_MODERATION_APPROVED;
            case REJECTED -> com.divyam.advent.service.AuditService.ACTION_MODERATION_REJECTED;
            case PENDING -> com.divyam.advent.service.AuditService.ACTION_MODERATION_PENDING;
        };
        auditService.record(actor(jwt), action,
                com.divyam.advent.service.AuditService.TARGET_USER_CHALLENGE, id, null);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/user-challenges/{id}/photo")
    public ResponseEntity<AdminUserChallengeDto> removePhoto(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        adminGuard.requireAdmin(getClerkUserId(jwt));
        UserChallenge uc = userChallengeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserChallenge not found with id: " + id));
        if (uc.getProofPhotoPublicId() != null && !uc.getProofPhotoPublicId().isBlank()) {
            cloudinaryCleanupService.destroyOnCloudinary(uc.getProofPhotoPublicId());
        }
        uc.setProofPhotoUrl(null);
        uc.setProofPhotoPublicId(null);
        // No photo means the proof is gone — treat as a denial so stats reflect
        // the loss and badges re-evaluate.
        uc.setModerationStatus(ModerationStatus.REJECTED);
        UserChallenge saved = userChallengeRepository.save(uc);
        if (saved.getUser() != null) {
            badgeService.evaluateAndAssignBadges(saved.getUser());
        }
        auditService.record(actor(jwt), com.divyam.advent.service.AuditService.ACTION_PHOTO_DELETED,
                com.divyam.advent.service.AuditService.TARGET_USER_CHALLENGE, id, null);
        return ResponseEntity.ok(toDto(saved));
    }

    // --- Audit log feed -----------------------------------------------------

    @GetMapping("/audit-log")
    public ResponseEntity<List<com.divyam.advent.dto.AdminAuditLogDto>> auditLog(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        adminGuard.requireAdmin(getClerkUserId(jwt));
        org.springframework.data.domain.PageRequest pageable = org.springframework.data.domain.PageRequest.of(
                Math.max(page, 0), Math.min(Math.max(size, 1), 200));
        List<com.divyam.advent.dto.AdminAuditLogDto> rows = auditLogRepository
                .findAllByOrderByCreatedAtDesc(pageable)
                .stream()
                .map(e -> new com.divyam.advent.dto.AdminAuditLogDto(
                        e.getId(), e.getActorUserId(), e.getActorName(), e.getAction(),
                        e.getTargetType(), e.getTargetId(), e.getReason(), e.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(rows);
    }

    private AdminUserSummaryDto toSummary(User u) {
        boolean superAdmin = adminGuard.isSuperAdmin(u.getAuthSubject());
        boolean admin = superAdmin || u.isAdmin();
        return new AdminUserSummaryDto(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getCountry(),
                u.getAvatar(),
                u.getBannerUrl(),
                userChallengeRepository.countByUser_IdAndStatusAndModerationStatus(
                        u.getId(), CompletionStatus.COMPLETED, ModerationStatus.APPROVED),
                userChallengeRepository.countByUser_IdAndStatus(u.getId(), CompletionStatus.ASSIGNED),
                admin,
                superAdmin,
                u.isCurrentlyBanned(),
                u.isCurrentlyBanned() ? u.getBanReason() : null,
                u.isCurrentlyBanned() ? u.getBanExpiresAt() : null
        );
    }

    private AdminUserChallengeDto toDto(UserChallenge uc) {
        return new AdminUserChallengeDto(
                uc.getId(),
                uc.getUser() != null ? uc.getUser().getId() : null,
                uc.getUser() != null ? uc.getUser().getName() : null,
                uc.getUser() != null ? uc.getUser().getEmail() : null,
                uc.getChallenge() != null ? uc.getChallenge().getId() : null,
                uc.getChallenge() != null ? uc.getChallenge().getTitle() : null,
                uc.getChallenge() != null && uc.getChallenge().getCategory() != null
                        ? uc.getChallenge().getCategory().name() : null,
                uc.getCompletionTime(),
                uc.getProofPhotoUrl(),
                uc.getUserReflection(),
                uc.getModerationStatus()
        );
    }

    private String getClerkUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().trim().isEmpty()) {
            throw new AccessDeniedException("Invalid authentication token");
        }
        return jwt.getSubject().trim();
    }
}
