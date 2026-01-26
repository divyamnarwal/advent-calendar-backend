package com.divyam.advent.service;

import com.divyam.advent.dto.TimeCapsuleRequestDto;
import com.divyam.advent.dto.TimeCapsuleResponseDto;
import com.divyam.advent.exception.ResourceNotFoundException;
import com.divyam.advent.model.TimeCapsule;
import com.divyam.advent.repository.TimeCapsuleRepository;
import com.divyam.advent.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of TimeCapsuleService.
 * Contains business logic for managing time capsules.
 */
@Service
public class TimeCapsuleServiceImpl implements TimeCapsuleService {

    private final TimeCapsuleRepository timeCapsuleRepository;
    private final UserRepository userRepository;

    @Autowired
    public TimeCapsuleServiceImpl(TimeCapsuleRepository timeCapsuleRepository,
                                  UserRepository userRepository) {
        this.timeCapsuleRepository = timeCapsuleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public TimeCapsuleResponseDto createCapsule(Long userId, TimeCapsuleRequestDto request) {
        // 1. Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // 2. Validate content is not empty
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }

        // 3. Calculate reveal date
        LocalDateTime revealDate = calculateRevealDate(request);

        // 4. Validate reveal date is in the future
        if (revealDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reveal date must be in the future");
        }

        // 5. Create and save the capsule
        TimeCapsule capsule = new TimeCapsule(userId, request.getContent(), revealDate);
        TimeCapsule savedCapsule = timeCapsuleRepository.save(capsule);

        // 6. Return response
        return toResponseDto(savedCapsule);
    }

    @Override
    public List<TimeCapsuleResponseDto> getRevealedCapsules(Long userId) {
        // 1. Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // 2. Get revealed capsules (reveal date has passed)
        LocalDateTime now = LocalDateTime.now();
        List<TimeCapsule> capsules = timeCapsuleRepository.findRevealedCapsules(userId, now);

        // 3. Mark them as revealed
        for (TimeCapsule capsule : capsules) {
            if (!capsule.isRevealed()) {
                capsule.setRevealed(true);
                timeCapsuleRepository.save(capsule);
            }
        }

        // 4. Convert to DTOs and return
        return capsules.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeCapsuleResponseDto> getPendingCapsules(Long userId) {
        // 1. Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // 2. Get pending capsules (reveal date in future)
        LocalDateTime now = LocalDateTime.now();
        List<TimeCapsule> capsules = timeCapsuleRepository.findPendingCapsules(userId, now);

        // 3. Convert to DTOs and return
        return capsules.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Calculates the reveal date from the request.
     * Uses explicit date if provided, otherwise calculates from daysUntilReveal.
     * Defaults to 7 days if neither is specified.
     */
    private LocalDateTime calculateRevealDate(TimeCapsuleRequestDto request) {
        if (request.getRevealDate() != null) {
            return request.getRevealDate();
        }

        int days = (request.getDaysUntilReveal() != null)
            ? request.getDaysUntilReveal()
            : 7;  // Default: 7 days

        return LocalDateTime.now().plusDays(days);
    }

    /**
     * Converts a TimeCapsule entity to a Response DTO.
     */
    private TimeCapsuleResponseDto toResponseDto(TimeCapsule capsule) {
        return new TimeCapsuleResponseDto(
                capsule.getId(),
                capsule.getContent(),
                capsule.getRevealDate(),
                capsule.getCreatedAt(),
                capsule.isRevealed(),
                capsule.isRevealable()
        );
    }
}
