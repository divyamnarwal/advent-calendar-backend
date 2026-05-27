package com.divyam.advent.service;

import com.divyam.advent.dto.PhotoCreateRequest;
import com.divyam.advent.dto.PhotoResponseDto;
import com.divyam.advent.dto.PhotoUploadSignatureResponse;
import com.divyam.advent.exception.PhotoLimitExceededException;
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
import java.time.ZoneId;
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
    private final int monthlyPhotoLimit;
    private final String uploadTransformation;

    public PhotoServiceImpl(
            PhotoRepository photoRepository,
            UserRepository userRepository,
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret,
            @Value("${cloudinary.folder}") String folder,
            @Value("${photo.monthly-limit:300}") int monthlyPhotoLimit,
            @Value("${cloudinary.upload-transformation:c_limit,w_1600,h_1600,q_auto:good}")
            String uploadTransformation
    ) {
        this.photoRepository = photoRepository;
        this.userRepository = userRepository;
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.folder = folder;
        this.monthlyPhotoLimit = monthlyPhotoLimit;
        this.uploadTransformation = uploadTransformation;
    }

    @Override
    public PhotoUploadSignatureResponse getUploadSignature(Long userId) {
        validateUser(userId);
        ensureCloudinaryConfigured();

        long timestamp = Instant.now().getEpochSecond();
        // Incoming transformation: Cloudinary applies it BEFORE storing, so the stored
        // master is already downscaled + quality-optimized (saves storage on the free tier).
        // Signed params must be sorted alphabetically: folder, timestamp, transformation.
        boolean hasTransformation = uploadTransformation != null && !uploadTransformation.trim().isEmpty();
        String transformation = hasTransformation ? uploadTransformation.trim() : null;

        StringBuilder payload = new StringBuilder("folder=").append(folder)
                .append("&timestamp=").append(timestamp);
        if (transformation != null) {
            payload.append("&transformation=").append(transformation);
        }
        String signature = signPayload(payload.toString());

        return new PhotoUploadSignatureResponse(
                cloudName,
                apiKey,
                folder,
                timestamp,
                signature,
                transformation
        );
    }

    @Override
    public PhotoResponseDto createPhoto(Long userId, PhotoCreateRequest request) {
        checkMonthlyLimit(userId);
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
    public PhotoLimitStatusResponse getMonthlyLimitStatus(Long userId) {
        validateUser(userId);

        long currentCount = getCurrentMonthPhotoCount(userId);
        int used = Math.toIntExact(currentCount);
        int remaining = Math.max(0, monthlyPhotoLimit - used);

        return new PhotoLimitStatusResponse(used, remaining, monthlyPhotoLimit);
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

    private void checkMonthlyLimit(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        long currentCount = getCurrentMonthPhotoCount(userId);
        if (currentCount >= monthlyPhotoLimit) {
            throw new PhotoLimitExceededException(currentCount, monthlyPhotoLimit);
        }
    }

    private long getCurrentMonthPhotoCount(Long userId) {
        YearMonth currentMonth = YearMonth.from(LocalDateTime.now(ZoneId.systemDefault()));
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.plusMonths(1).atDay(1).atStartOfDay().minusNanos(1);

        return photoRepository.countByUserIdAndCreatedAtBetween(userId, monthStart, monthEnd);
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
