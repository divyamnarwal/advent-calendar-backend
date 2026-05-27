package com.divyam.advent.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ProfileUpdateRequestDto {

    private String name;
    private String avatar;
    private String avatarPublicId;
    private String bio;
    private String accentColor;
    private String featuredBadgeId;
    private String bannerUrl;
    private String bannerPublicId;
    private String country;
    private String socialVk;
    private String socialTelegram;
    private String socialWhatsapp;
    private String socialInstagram;
    private String socialTwitter;
    private boolean nameProvided;
    private boolean avatarProvided;
    private boolean avatarPublicIdProvided;
    private boolean bioProvided;
    private boolean accentColorProvided;
    private boolean featuredBadgeIdProvided;
    private boolean bannerUrlProvided;
    private boolean bannerPublicIdProvided;
    private boolean countryProvided;
    private boolean socialVkProvided;
    private boolean socialTelegramProvided;
    private boolean socialWhatsappProvided;
    private boolean socialInstagramProvided;
    private boolean socialTwitterProvided;

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

    public String getAvatarPublicId() {
        return avatarPublicId;
    }

    public void setAvatarPublicId(String avatarPublicId) {
        this.avatarPublicId = avatarPublicId;
        this.avatarPublicIdProvided = true;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
        this.bioProvided = true;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
        this.accentColorProvided = true;
    }

    public String getFeaturedBadgeId() {
        return featuredBadgeId;
    }

    public void setFeaturedBadgeId(String featuredBadgeId) {
        this.featuredBadgeId = featuredBadgeId;
        this.featuredBadgeIdProvided = true;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
        this.bannerUrlProvided = true;
    }

    public String getBannerPublicId() {
        return bannerPublicId;
    }

    public void setBannerPublicId(String bannerPublicId) {
        this.bannerPublicId = bannerPublicId;
        this.bannerPublicIdProvided = true;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
        this.countryProvided = true;
    }

    public String getSocialVk() {
        return socialVk;
    }

    public void setSocialVk(String socialVk) {
        this.socialVk = socialVk;
        this.socialVkProvided = true;
    }

    public String getSocialTelegram() {
        return socialTelegram;
    }

    public void setSocialTelegram(String socialTelegram) {
        this.socialTelegram = socialTelegram;
        this.socialTelegramProvided = true;
    }

    public String getSocialWhatsapp() {
        return socialWhatsapp;
    }

    public void setSocialWhatsapp(String socialWhatsapp) {
        this.socialWhatsapp = socialWhatsapp;
        this.socialWhatsappProvided = true;
    }

    public String getSocialInstagram() {
        return socialInstagram;
    }

    public void setSocialInstagram(String socialInstagram) {
        this.socialInstagram = socialInstagram;
        this.socialInstagramProvided = true;
    }

    public String getSocialTwitter() {
        return socialTwitter;
    }

    public void setSocialTwitter(String socialTwitter) {
        this.socialTwitter = socialTwitter;
        this.socialTwitterProvided = true;
    }

    @JsonIgnore
    public boolean isNameProvided() {
        return nameProvided;
    }

    @JsonIgnore
    public boolean isAvatarProvided() {
        return avatarProvided;
    }

    @JsonIgnore
    public boolean isAvatarPublicIdProvided() {
        return avatarPublicIdProvided;
    }

    @JsonIgnore
    public boolean isBioProvided() {
        return bioProvided;
    }

    @JsonIgnore
    public boolean isAccentColorProvided() {
        return accentColorProvided;
    }

    @JsonIgnore
    public boolean isFeaturedBadgeIdProvided() {
        return featuredBadgeIdProvided;
    }

    @JsonIgnore
    public boolean isBannerUrlProvided() {
        return bannerUrlProvided;
    }

    @JsonIgnore
    public boolean isBannerPublicIdProvided() {
        return bannerPublicIdProvided;
    }

    @JsonIgnore
    public boolean isCountryProvided() {
        return countryProvided;
    }

    @JsonIgnore
    public boolean isSocialVkProvided() {
        return socialVkProvided;
    }

    @JsonIgnore
    public boolean isSocialTelegramProvided() {
        return socialTelegramProvided;
    }

    @JsonIgnore
    public boolean isSocialWhatsappProvided() {
        return socialWhatsappProvided;
    }

    @JsonIgnore
    public boolean isSocialInstagramProvided() {
        return socialInstagramProvided;
    }

    @JsonIgnore
    public boolean isSocialTwitterProvided() {
        return socialTwitterProvided;
    }
}
