package com.divyam.advent.model;

import com.divyam.advent.enums.ChallengeCategory;
import com.divyam.advent.enums.Culture;
import com.divyam.advent.enums.EnergyLevel;
import jakarta.persistence.*;

@Entity
@Table(name = "challenges")
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    /** Russian title; nullable. Frontend falls back to {@link #title} when empty. */
    @Column(name = "title_ru")
    private String titleRu;

    /** Russian description; nullable. Frontend falls back to {@link #description} when empty. */
    @Column(name = "description_ru", length = 2000)
    private String descriptionRu;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ChallengeCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "energy_level", nullable = false)
    private EnergyLevel energyLevel;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Enumerated(EnumType.STRING)
    @Column(name = "culture", nullable = false)
    private Culture culture = Culture.GLOBAL;

    @Column(name = "cycle_day")
    private Integer cycleDay;

    @Column(name = "source_version")
    private String sourceVersion;

    /**
     * Month (1-12) this challenge is bound to, for date-specific / holiday challenges.
     * Together with {@link #eventDay} it makes the challenge appear only on that
     * calendar day, recurring every year. Null for regular (undated) challenges.
     */
    @Column(name = "event_month")
    private Integer eventMonth;

    /**
     * Day of month (1-31) this challenge is bound to. See {@link #eventMonth}.
     */
    @Column(name = "event_day")
    private Integer eventDay;

    /**
     * Optional time limit (in minutes) for this challenge. Counted from the
     * UserChallenge {@code startTime}. Null = no limit. If set and the user
     * tries to complete after now > startTime + durationMinutes, completion
     * is rejected.
     */
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    public Challenge() {
    }

    public Challenge(String title, String description, ChallengeCategory category, EnergyLevel energyLevel, boolean active) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.energyLevel = energyLevel;
        this.active = active;
    }

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

    public String getTitleRu() {
        return titleRu;
    }

    public void setTitleRu(String titleRu) {
        this.titleRu = titleRu;
    }

    public String getDescriptionRu() {
        return descriptionRu;
    }

    public void setDescriptionRu(String descriptionRu) {
        this.descriptionRu = descriptionRu;
    }

    public ChallengeCategory getCategory() {
        return category;
    }

    public void setCategory(ChallengeCategory category) {
        this.category = category;
    }

    public EnergyLevel getEnergyLevel() {
        return energyLevel;
    }

    public void setEnergyLevel(EnergyLevel energyLevel) {
        this.energyLevel = energyLevel;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Culture getCulture() {
        return culture;
    }

    public void setCulture(Culture culture) {
        this.culture = culture;
    }

    public Integer getCycleDay() {
        return cycleDay;
    }

    public void setCycleDay(Integer cycleDay) {
        this.cycleDay = cycleDay;
    }

    public String getSourceVersion() {
        return sourceVersion;
    }

    public void setSourceVersion(String sourceVersion) {
        this.sourceVersion = sourceVersion;
    }

    public Integer getEventMonth() {
        return eventMonth;
    }

    public void setEventMonth(Integer eventMonth) {
        this.eventMonth = eventMonth;
    }

    public Integer getEventDay() {
        return eventDay;
    }

    public void setEventDay(Integer eventDay) {
        this.eventDay = eventDay;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    /** True when this challenge is pinned to a specific calendar day (holiday). */
    public boolean isDateBound() {
        return eventMonth != null && eventDay != null;
    }
}
