package com.divyam.advent.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class RecapPhotoPreviewDto {
    private Long id;
    private String secureUrl;
    private String caption;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public RecapPhotoPreviewDto() {
    }

    public RecapPhotoPreviewDto(Long id, String secureUrl, String caption, LocalDateTime createdAt) {
        this.id = id;
        this.secureUrl = secureUrl;
        this.caption = caption;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getSecureUrl() {
        return secureUrl;
    }

    public String getCaption() {
        return caption;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
