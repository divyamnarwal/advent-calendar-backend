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
}
