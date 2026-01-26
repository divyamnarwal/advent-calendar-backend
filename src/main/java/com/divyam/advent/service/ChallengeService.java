package com.divyam.advent.service;

import com.divyam.advent.enums.ChallengeCategory;
import com.divyam.advent.model.Challenge;
import java.util.List;

public interface ChallengeService {

    Challenge createChallenge(Challenge challenge);

    List<Challenge> getAllChallenges();

    List<Challenge> getActiveChallengesByCategory(ChallengeCategory category);
}
