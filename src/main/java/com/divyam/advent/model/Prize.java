package com.divyam.advent.model;

import com.divyam.advent.enums.PrizeCriteria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * An admin-configurable prize. Winners are ranked by {@link PrizeCriteria} over an optional
 * period month, and awarded to a user by an admin (hybrid flow). Table is created by
 * {@code ddl-auto=update} from this entity.
 */
@Entity
@Table(name = "prizes")
public class Prize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "criteria", nullable = false)
    private PrizeCriteria criteria;

    /** Required only for {@link PrizeCriteria#FASTEST_CHALLENGE}. */
    @Column(name = "target_challenge_id")
    private Long targetChallengeId;

    /** "YYYY-MM"; null means the current month / ongoing. */
    @Column(name = "period_month")
    private String periodMonth;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "awarded_user_id")
    private Long awardedUserId;

    @Column(name = "awarded_at")
    private LocalDateTime awardedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PrizeCriteria getCriteria() {
        return criteria;
    }

    public void setCriteria(PrizeCriteria criteria) {
        this.criteria = criteria;
    }

    public Long getTargetChallengeId() {
        return targetChallengeId;
    }

    public void setTargetChallengeId(Long targetChallengeId) {
        this.targetChallengeId = targetChallengeId;
    }

    public String getPeriodMonth() {
        return periodMonth;
    }

    public void setPeriodMonth(String periodMonth) {
        this.periodMonth = periodMonth;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getAwardedUserId() {
        return awardedUserId;
    }

    public void setAwardedUserId(Long awardedUserId) {
        this.awardedUserId = awardedUserId;
    }

    public LocalDateTime getAwardedAt() {
        return awardedAt;
    }

    public void setAwardedAt(LocalDateTime awardedAt) {
        this.awardedAt = awardedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
