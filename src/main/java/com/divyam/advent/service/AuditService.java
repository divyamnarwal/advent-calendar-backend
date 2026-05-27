package com.divyam.advent.service;

import com.divyam.advent.model.AdminAuditLog;
import com.divyam.advent.model.User;
import com.divyam.advent.repository.AdminAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Append-only audit trail for admin-initiated mutations. Persistence failures
 * are logged but never propagated — losing one log line is preferred to
 * blocking a legitimate admin action.
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    public static final String ACTION_USER_BANNED = "USER_BANNED";
    public static final String ACTION_USER_UNBANNED = "USER_UNBANNED";
    public static final String ACTION_ADMIN_GRANTED = "ADMIN_GRANTED";
    public static final String ACTION_ADMIN_REVOKED = "ADMIN_REVOKED";
    public static final String ACTION_MODERATION_APPROVED = "MODERATION_APPROVED";
    public static final String ACTION_MODERATION_REJECTED = "MODERATION_REJECTED";
    public static final String ACTION_MODERATION_PENDING = "MODERATION_PENDING";
    public static final String ACTION_PHOTO_DELETED = "PHOTO_DELETED";
    public static final String ACTION_AVATAR_CLEARED = "AVATAR_CLEARED";
    public static final String ACTION_BANNER_CLEARED = "BANNER_CLEARED";

    public static final String TARGET_USER = "USER";
    public static final String TARGET_USER_CHALLENGE = "USER_CHALLENGE";

    private final AdminAuditLogRepository repository;

    public AuditService(AdminAuditLogRepository repository) {
        this.repository = repository;
    }

    public void record(User actor, String action, String targetType, Long targetId, String reason) {
        try {
            AdminAuditLog entry = new AdminAuditLog(
                    actor != null ? actor.getId() : null,
                    actor != null ? actor.getName() : null,
                    action,
                    targetType,
                    targetId,
                    reason
            );
            repository.save(entry);
        } catch (RuntimeException ex) {
            // Don't fail the admin action just because the audit row didn't
            // persist — log loudly so it's visible in monitoring.
            log.error("Failed to write admin audit entry action={} target={}:{} reason={}",
                    action, targetType, targetId, reason, ex);
        }
    }
}
