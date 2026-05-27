package com.divyam.advent.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Single row per admin-initiated mutation: ban, moderation decision, role
 * grant/revoke, asset deletion. Append-only — never updated or removed by app
 * code. Source of truth for "who changed what, when, and why".
 */
@Entity
@Table(
        name = "admin_audit_log",
        indexes = {
                @Index(name = "idx_audit_created_at", columnList = "created_at"),
                @Index(name = "idx_audit_actor", columnList = "actor_user_id"),
                @Index(name = "idx_audit_target", columnList = "target_type,target_id")
        }
)
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** User.id of the admin who performed the action. */
    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    /** Optional display name snapshot — survives if the actor is later renamed/deleted. */
    @Column(name = "actor_name", length = 255)
    private String actorName;

    /** Stable action code, e.g. USER_BANNED, MODERATION_APPROVED, PHOTO_DELETED. */
    @Column(name = "action", nullable = false, length = 64)
    private String action;

    /** Domain object kind: USER, USER_CHALLENGE, PRIZE… */
    @Column(name = "target_type", length = 64)
    private String targetType;

    /** Domain object id (foreign key not enforced — log survives row deletion). */
    @Column(name = "target_id")
    private Long targetId;

    /** Admin-supplied reason / extra context. */
    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public AdminAuditLog() {}

    public AdminAuditLog(Long actorUserId, String actorName, String action,
                         String targetType, Long targetId, String reason) {
        this.actorUserId = actorUserId;
        this.actorName = actorName;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getActorUserId() { return actorUserId; }
    public String getActorName() { return actorName; }
    public String getAction() { return action; }
    public String getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public String getReason() { return reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
