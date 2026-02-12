package com.divyam.advent.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ProfileUpdateRequestDto {

    private String name;
    private String avatar;
    private boolean nameProvided;
    private boolean avatarProvided;

    public ProfileUpdateRequestDto() {
    }

    public ProfileUpdateRequestDto(String name, String avatar) {
        this.name = name;
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.nameProvided = true;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
        this.avatarProvided = true;
    }

    @JsonIgnore
    public boolean isNameProvided() {
        return nameProvided;
    }

    @JsonIgnore
    public boolean isAvatarProvided() {
        return avatarProvided;
    }
}
