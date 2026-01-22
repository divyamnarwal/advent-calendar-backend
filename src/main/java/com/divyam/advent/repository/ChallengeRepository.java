package com.divyam.advent.repository;

import com.divyam.advent.enums.ChallengeCategory;
import com.divyam.advent.enums.Culture;
import com.divyam.advent.enums.EnergyLevel;
import com.divyam.advent.model.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    List<Challenge> findByCategoryAndActiveTrue(ChallengeCategory category);

    /**
     * Find all active challenges.
     * @return list of all active challenges
     */
    List<Challenge> findByActiveTrue();

    /**
     * Find active challenges by energy level.
     * Used to match challenges to user's mood.
     * @param energyLevel the energy level (LOW, MEDIUM, HIGH)
     * @return list of active challenges with that energy level
     */
    List<Challenge> findByEnergyLevelAndActiveTrue(EnergyLevel energyLevel);

    /**
     * Find active challenges by category AND energy level.
     * Used for mood-based daily challenge selection.
     * @param category the challenge category
     * @param energyLevel the energy level (LOW, MEDIUM, HIGH)
     * @return list of active challenges matching both criteria
     */
    List<Challenge> findByCategoryAndEnergyLevelAndActiveTrue(ChallengeCategory category, EnergyLevel energyLevel);

    /**
     * Find active challenges by culture.
     * Used to get challenges matching user's country.
     * @param culture the culture (INDIA, RUSSIA, GLOBAL)
     * @return list of active challenges with that culture
     */
    List<Challenge> findByCultureAndActiveTrue(Culture culture);

    /**
     * Find active challenges by culture AND energy level.
     * Used for mood-based daily challenge selection with cultural context.
     * @param culture the culture (INDIA, RUSSIA, GLOBAL)
     * @param energyLevel the energy level (LOW, MEDIUM, HIGH)
     * @return list of active challenges matching both criteria
     */
    List<Challenge> findByCultureAndEnergyLevelAndActiveTrue(Culture culture, EnergyLevel energyLevel);

    /**
     * Find challenges from OTHER cultures (excluding specified culture).
     * Used for cross-cultural challenge days.
     * @param culture the culture to exclude
     * @return list of active challenges from different cultures
     */
    @Query("SELECT c FROM Challenge c WHERE c.culture != :culture AND c.active = true")
    List<Challenge> findCrossCulturalChallenges(@Param("culture") Culture culture);
}
