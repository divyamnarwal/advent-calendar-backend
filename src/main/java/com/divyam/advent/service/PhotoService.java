package com.divyam.advent.service;

import com.divyam.advent.dto.PhotoCreateRequest;
import com.divyam.advent.dto.PhotoResponseDto;
import com.divyam.advent.dto.PhotoUploadSignatureResponse;

import java.time.YearMonth;
import java.util.List;

public interface PhotoService {
    record PhotoLimitStatusResponse(int used, int remaining, int limit) {}

    PhotoUploadSignatureResponse getUploadSignature(Long userId);

    PhotoResponseDto createPhoto(Long userId, PhotoCreateRequest request);

    List<PhotoResponseDto> getPhotos(Long userId, YearMonth month);

    PhotoLimitStatusResponse getMonthlyLimitStatus(Long userId);

    void deletePhoto(Long userId, Long photoId);
}
