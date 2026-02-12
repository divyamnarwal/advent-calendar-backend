package com.divyam.advent.service;

import com.divyam.advent.dto.PhotoCreateRequest;
import com.divyam.advent.dto.PhotoResponseDto;
import com.divyam.advent.dto.PhotoUploadSignatureResponse;
import com.divyam.advent.exception.ResourceNotFoundException;
import com.divyam.advent.model.Photo;
import com.divyam.advent.repository.PhotoRepository;
import com.divyam.advent.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PhotoServiceImpl implements PhotoService {

    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final String folder;

    public PhotoServiceImpl(
            PhotoRepository photoRepository,
            UserRepository userRepository,
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret,
            @Value("${cloudinary.folder}") String folder
    ) {
        this.photoRepository = photoRepository;
        this.userRepository = userRepository;
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.folder = folder;
    }

    @Override
    public PhotoUploadSignatureResponse getUploadSignature(Long userId) {
        validateUser(userId);
        ensureCloudinaryConfigured();

        long timestamp = Instant.now().getEpochSecond();
        String payload = "folder=" + folder + "&timestamp=" + timestamp;
        String signature = signPayload(payload);

        return new PhotoUploadSignatureResponse(
                cloudName,
                apiKey,
                folder,
                timestamp,
                signature
        );
    }

    @Override
    public PhotoResponseDto createPhoto(Long userId, PhotoCreateRequest request) {
        validateUser(userId);

        if (request == null) {
            throw new IllegalArgumentException("Photo request is required");
        }

        Photo photo = new Photo();
        photo.setUserId(userId);
        photo.setPublicId(request.getPublicId().trim());
        photo.setSecureUrl(request.getSecureUrl().trim());
        photo.setCaption(normalizeText(request.getCaption()));
        photo.setFormat(normalizeText(request.getFormat()));
        photo.setWidth(request.getWidth());
        photo.setHeight(request.getHeight());
        photo.setBytes(request.getBytes());
        photo.setTakenAt(request.getTakenAt());
        photo.setCreatedAt(LocalDateTime.now());

        Photo saved = photoRepository.save(photo);
        return toDto(saved);
    }

    @Override
    public List<PhotoResponseDto> getPhotos(Long userId, YearMonth month) {
        validateUser(userId);

        YearMonth targetMonth = month != null ? month : YearMonth.now();
        LocalDateTime monthStart = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = targetMonth.plusMonths(1).atDay(1).atStartOfDay().minusNanos(1);

        return photoRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                        userId,
                        monthStart,
                        monthEnd
                )
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePhoto(Long userId, Long photoId) {
        validateUser(userId);
        Photo photo = photoRepository.findByIdAndUserId(photoId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));
        photoRepository.delete(photo);
    }

    private void validateUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    private void ensureCloudinaryConfigured() {
        if (isBlank(cloudName) || isBlank(apiKey) || isBlank(apiSecret)) {
            throw new IllegalStateException("Cloudinary is not configured on the server");
        }
    }

    private String signPayload(String payload) {
        try {
            String valueToSign = payload + apiSecret;
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] digest = sha1.digest(valueToSign.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate Cloudinary signature", ex);
        }
    }

    private PhotoResponseDto toDto(Photo photo) {
        return new PhotoResponseDto(
                photo.getId(),
                photo.getUserId(),
                photo.getPublicId(),
                photo.getSecureUrl(),
                photo.getCaption(),
                photo.getFormat(),
                photo.getWidth(),
                photo.getHeight(),
                photo.getBytes(),
                photo.getTakenAt(),
                photo.getCreatedAt()
        );
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
