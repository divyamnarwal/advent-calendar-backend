package com.divyam.advent.service.impl;

import com.divyam.advent.dto.ChallengeCycleDayDto;
import com.divyam.advent.enums.ChallengeCategory;
import com.divyam.advent.model.Challenge;
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
