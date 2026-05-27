package com.divyam.advent.service.impl;

import com.divyam.advent.dto.ChallengeCycleDayDto;
import com.divyam.advent.enums.ChallengeCategory;
import com.divyam.advent.model.Challenge;
import com.divyam.advent.exception.ResourceNotFoundException;
import com.divyam.advent.repository.ChallengeRepository;
import com.divyam.advent.service.ChallengeCycleSyncService;
import com.divyam.advent.service.ChallengeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChallengeServiceImpl implements ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeCycleSyncService challengeCycleSyncService;

    public ChallengeServiceImpl(
            ChallengeRepository challengeRepository,
            ChallengeCycleSyncService challengeCycleSyncService
    ) {
        this.challengeRepository = challengeRepository;
        this.challengeCycleSyncService = challengeCycleSyncService;
    }

    @Override
    public Challenge createChallenge(Challenge challenge) {
        if (challenge == null) {
            throw new IllegalArgumentException("Challenge cannot be null");
        }
        return challengeRepository.save(challenge);
    }

    @Override
    public Challenge updateChallenge(Long id, Challenge updates) {
        if (updates == null) {
            throw new IllegalArgumentException("Challenge updates cannot be null");
        }
        Challenge existing = challengeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found with id: " + id));

        if (updates.getTitle() != null) existing.setTitle(updates.getTitle());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getTitleRu() != null) existing.setTitleRu(updates.getTitleRu());
        if (updates.getDescriptionRu() != null) existing.setDescriptionRu(updates.getDescriptionRu());
        if (updates.getCategory() != null) existing.setCategory(updates.getCategory());
        if (updates.getEnergyLevel() != null) existing.setEnergyLevel(updates.getEnergyLevel());
        if (updates.getCulture() != null) existing.setCulture(updates.getCulture());
        existing.setActive(updates.isActive());
        if (updates.getEventMonth() != null) existing.setEventMonth(updates.getEventMonth());
        if (updates.getEventDay() != null) existing.setEventDay(updates.getEventDay());
        if (updates.getDurationMinutes() != null) existing.setDurationMinutes(updates.getDurationMinutes());

        return challengeRepository.save(existing);
    }

    @Override
    public void deleteChallenge(Long id) {
        if (!challengeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Challenge not found with id: " + id);
        }
        challengeRepository.deleteById(id);
    }

    @Override
    public List<Challenge> getAllChallenges() {
        return challengeRepository.findAll();
    }

    @Override
    public List<Challenge> getActiveChallengesByCategory(ChallengeCategory category) {
        return challengeRepository.findByCategoryAndActiveTrue(category);
    }

    @Override
    public List<ChallengeCycleDayDto> getCurrentCyclePlan() {
        return challengeCycleSyncService.getCurrentCycleChallenges().stream()
                .map(this::toCycleDayDto)
                .toList();
    }

    private ChallengeCycleDayDto toCycleDayDto(Challenge challenge) {
        return new ChallengeCycleDayDto(
                challenge.getCycleDay(),
                challenge.getTitle(),
                challenge.getDescription(),
                switch (challenge.getEnergyLevel()) {
                    case HIGH -> "Hard";
                    case MEDIUM -> "Medium";
                    case LOW -> "Easy";
                }
        );
    }
}
