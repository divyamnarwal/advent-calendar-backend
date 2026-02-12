package com.divyam.advent.service;

import com.divyam.advent.dto.PhotoCreateRequest;
import com.divyam.advent.dto.PhotoResponseDto;
import com.divyam.advent.dto.PhotoUploadSignatureResponse;

import java.time.YearMonth;
import java.util.List;

public interface PhotoService {
    PhotoUploadSignatureResponse getUploadSignature(Long userId);

    PhotoResponseDto createPhoto(Long userId, PhotoCreateRequest request);

    List<PhotoResponseDto> getPhotos(Long userId, YearMonth month);

    void deletePhoto(Long userId, Long photoId);
}
