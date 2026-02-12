package com.divyam.advent.dto;

import java.time.LocalDateTime;

public class PhotoResponseDto {
    private Long id;
    private Long userId;
    private String publicId;
    private String secureUrl;
    private String caption;
    private String format;
    private Integer width;
    private Integer height;
    private Long bytes;
    private LocalDateTime takenAt;
    private LocalDateTime createdAt;

    public PhotoResponseDto() {
    }

    public PhotoResponseDto(
            Long id,
            Long userId,
            String publicId,
            String secureUrl,
            String caption,
            String format,
            Integer width,
            Integer height,
            Long bytes,
            LocalDateTime takenAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.publicId = publicId;
        this.secureUrl = secureUrl;
        this.caption = caption;
        this.format = format;
        this.width = width;
        this.height = height;
        this.bytes = bytes;
        this.takenAt = takenAt;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getSecureUrl() {
        return secureUrl;
    }

    public void setSecureUrl(String secureUrl) {
        this.secureUrl = secureUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    public LocalDateTime getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(LocalDateTime takenAt) {
        this.takenAt = takenAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
