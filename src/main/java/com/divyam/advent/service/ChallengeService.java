package com.divyam.advent.service;

import com.divyam.advent.dto.ChallengeCycleDayDto;
import com.divyam.advent.enums.ChallengeCategory;
import com.divyam.advent.model.Challenge;
import java.util.List;

public interface ChallengeService {

    Challenge createChallenge(Challenge challenge);

    Challenge updateChallenge(Long id, Challenge updates);

    void deleteChallenge(Long id);

    List<Challenge> getAllChallenges();

    List<Challenge> getActiveChallengesByCategory(ChallengeCategory category);

    List<ChallengeCycleDayDto> getCurrentCyclePlan();
}
