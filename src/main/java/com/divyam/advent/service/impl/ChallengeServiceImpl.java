package com.divyam.advent.service.impl;

import com.divyam.advent.enums.ChallengeCategory;
import com.divyam.advent.model.Challenge;
import com.divyam.advent.repository.ChallengeRepository;
import com.divyam.advent.service.ChallengeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChallengeServiceImpl implements ChallengeService {

    private final ChallengeRepository challengeRepository;

    public ChallengeServiceImpl(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
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
}
