package com.divyam.advent.dto;

public class ProfileBadgeDto {

    private String id;
    private String title;
    private String description;
    private String icon;
    private String criteria;
    private boolean earned;
    private boolean newlyUnlocked;

    public ProfileBadgeDto() {
    }

    public ProfileBadgeDto(
            String id,
            String title,
            String description,
            String icon,
            String criteria,
            boolean earned,
            boolean newlyUnlocked
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.criteria = criteria;
        this.earned = earned;
        this.newlyUnlocked = newlyUnlocked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public boolean isEarned() {
        return earned;
    }

    public void setEarned(boolean earned) {
        this.earned = earned;
    }

    public boolean isNewlyUnlocked() {
        return newlyUnlocked;
    }

    public void setNewlyUnlocked(boolean newlyUnlocked) {
        this.newlyUnlocked = newlyUnlocked;
    }
}
