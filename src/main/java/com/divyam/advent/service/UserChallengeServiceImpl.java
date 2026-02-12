package com.divyam.advent.service;

import com.divyam.advent.dto.UserProgressDto;
import com.divyam.advent.enums.ChallengeCategory;
import com.divyam.advent.enums.CompletionStatus;
import com.divyam.advent.enums.Culture;
import com.divyam.advent.enums.EnergyLevel;
import com.divyam.advent.enums.Mood;
import com.divyam.advent.exception.ResourceNotFoundException;
import com.divyam.advent.model.Challenge;
import com.divyam.advent.model.User;
import com.divyam.advent.model.UserChallenge;
import com.divyam.advent.repository.ChallengeRepository;
import com.divyam.advent.repository.UserChallengeRepository;
import com.divyam.advent.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of UserChallengeService.
 * Contains the business logic for managing user-challenge participation.
 */
@Service
public class UserChallengeServiceImpl implements UserChallengeService {

    private final UserChallengeRepository userChallengeRepository;
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final BadgeService badgeService;
    private final ConcurrentHashMap<PreviewKey, Challenge> previewCache = new ConcurrentHashMap<>();

    private record PreviewKey(Long userId, LocalDate date, Mood mood) {
    }

    @Autowired
    public UserChallengeServiceImpl(UserChallengeRepository userChallengeRepository,
                                     UserRepository userRepository,
                                     ChallengeRepository challengeRepository,
                                     BadgeService badgeService) {
        this.userChallengeRepository = userChallengeRepository;
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
        this.badgeService = badgeService;
    }

    @Override
    public UserChallenge joinChallenge(Long userId, Long challengeId) {
        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if challenge exists
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found with id: " + challengeId));

        // Idempotent join: if user already has this challenge, return existing row
        UserChallenge existing = userChallengeRepository
                .findByUser_IdAndChallenge_Id(userId, challengeId)
                .orElse(null);
        if (existing != null) {
            return existing;
        }

        // Create new UserChallenge with ASSIGNED status
        UserChallenge userChallenge = new UserChallenge(user, challenge, CompletionStatus.ASSIGNED);

        return userChallengeRepository.save(userChallenge);
    }

    @Override
    public List<UserChallenge> getUserChallenges(Long userId) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return userChallengeRepository.findByUser_Id(userId);
    }

    @Override
    public List<UserChallenge> getChallengeParticipants(Long challengeId) {
        // Verify challenge exists
        if (!challengeRepository.existsById(challengeId)) {
            throw new ResourceNotFoundException("Challenge not found with id: " + challengeId);
        }

        return userChallengeRepository.findByChallenge_Id(challengeId);
    }

    @Override
    public UserChallenge getUserChallengeById(Long id) {
        return userChallengeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserChallenge not found with id: " + id));
    }

    @Override
    public UserChallenge markAsCompleted(Long id) {
        // 1. Fetch the UserChallenge - will throw ResourceNotFoundException if not found
        // This prevents completing unassigned challenges (non-existent UserChallenge)
        UserChallenge userChallenge = getUserChallengeById(id);

        // 2. State transition validation - only ASSIGNED can become COMPLETED
        // This prevents double-completion and invalid state transitions
        if (userChallenge.getStatus() != CompletionStatus.ASSIGNED) {
            throw new IllegalStateException(
                "Cannot complete challenge with status: " + userChallenge.getStatus() +
                ". Only ASSIGNED challenges can be marked as COMPLETED."
            );
        }

        // 3. Update status and set completion time (atomic operation)
        userChallenge.setStatus(CompletionStatus.COMPLETED);
        userChallenge.setCompletionTime(LocalDateTime.now());

        // 4. Save and refresh badge/stats state
        UserChallenge saved = userChallengeRepository.save(userChallenge);
        badgeService.evaluateAndAssignBadges(saved.getUser());
        return saved;
    }

    @Override
    public UserChallenge updateStatus(Long id, CompletionStatus status) {
        UserChallenge userChallenge = getUserChallengeById(id);
        userChallenge.setStatus(status);

        // If marking as completed, set completion time and refresh badges
        if (status == CompletionStatus.COMPLETED) {
            userChallenge.setCompletionTime(LocalDateTime.now());
        }

        UserChallenge saved = userChallengeRepository.save(userChallenge);
        if (status == CompletionStatus.COMPLETED) {
            badgeService.evaluateAndAssignBadges(saved.getUser());
        }
        return saved;
    }

    @Override
    public List<UserChallenge> getUserChallengesByStatus(Long userId, CompletionStatus status) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return userChallengeRepository.findByUser_IdAndStatus(userId, status);
    }

    @Override
    public UserProgressDto getUserProgress(Long userId) {
        // 1. Validate user exists and get User entity
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // 2. Get total assigned challenges (efficient COUNT query at database level)
        // This counts ALL challenges for the user (ASSIGNED + COMPLETED)
        long totalAssigned = userChallengeRepository.countByUser_Id(userId);

        // 3. Get completed challenges only (efficient COUNT query at database level)
        long totalCompleted = userChallengeRepository.countByUser_IdAndStatus(
                userId, CompletionStatus.COMPLETED
        );

        // 4. Build and return DTO (calculates percentage internally with division-by-zero protection)
        return new UserProgressDto(userId, user.getName(), totalAssigned, totalCompleted);
    }

    @Override
    public Challenge previewDailyChallenge(Long userId, Mood mood) {
        if (userId == null || mood == null) {
            throw new IllegalArgumentException("userId and mood are required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();

        List<UserChallenge> existingToday = userChallengeRepository
                .findByUser_IdAndStartTimeAfterAndStatus(userId, startOfToday, CompletionStatus.ASSIGNED);

        if (!existingToday.isEmpty()) {
            return existingToday.get(0).getChallenge();
        }

        PreviewKey key = new PreviewKey(userId, today, mood);
        Challenge cached = previewCache.get(key);
        if (cached != null) {
            return cached;
        }

        Challenge selected = selectDailyChallenge(user, mood);
        previewCache.put(key, selected);
        return selected;
    }

    @Override
    public UserChallenge confirmDailyChallenge(Long userId, Long challengeId, Mood mood) {
        if (userId == null || challengeId == null || mood == null) {
            throw new IllegalArgumentException("userId, challengeId, and mood are required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();

        List<UserChallenge> existingToday = userChallengeRepository
                .findByUser_IdAndStartTimeAfterAndStatus(userId, startOfToday, CompletionStatus.ASSIGNED);

        if (!existingToday.isEmpty()) {
            UserChallenge existing = existingToday.get(0);
            existing.setMood(mood);
            return userChallengeRepository.save(existing);
        }

        PreviewKey key = new PreviewKey(userId, today, mood);
        Challenge expected = previewCache.get(key);
        if (expected == null) {
            expected = selectDailyChallenge(user, mood);
        }

        if (expected.getId() == null || !expected.getId().equals(challengeId)) {
            throw new IllegalArgumentException("Preview mismatch or expired. Please preview again.");
        }

        UserChallenge userChallenge = new UserChallenge(user, expected, CompletionStatus.ASSIGNED);
        userChallenge.setMood(mood);
        UserChallenge saved = userChallengeRepository.save(userChallenge);
        previewCache.remove(key);
        return saved;
    }

    @Override
    public UserChallenge getOrAssignDailyChallenge(Long userId) {
        // 1. Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // 2. Define today's date range
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();

        // 3. Check if user already has a challenge assigned today
        List<UserChallenge> existingToday = userChallengeRepository
                .findByUser_IdAndStartTimeAfterAndStatus(userId, startOfToday, CompletionStatus.ASSIGNED);

        if (!existingToday.isEmpty()) {
            return existingToday.get(0);  // Already has today's challenge - return it
        }

        // 4. Get yesterday's challenge to determine its category (for avoidance)
        ChallengeCategory yesterdayCategory = getYesterdayCategory(userId, startOfToday);

        // 5. Get all active challenges
        List<Challenge> activeChallenges = challengeRepository.findByActiveTrue();

        if (activeChallenges.isEmpty()) {
            throw new IllegalStateException("No active challenges available");
        }

        // 6. Filter out yesterday's category to avoid repetition
        List<Challenge> availableChallenges = filterOutCategory(activeChallenges, yesterdayCategory);

        // 7. Fallback: if no challenges left after filtering, use all active challenges
        if (availableChallenges.isEmpty()) {
            availableChallenges = activeChallenges;
        }

        // 8. Randomly select one challenge
        Random random = new Random();
        Challenge selectedChallenge = availableChallenges.get(
                random.nextInt(availableChallenges.size())
        );

        // 9. Assign it to the user with ASSIGNED status
        UserChallenge userChallenge = new UserChallenge(user, selectedChallenge, CompletionStatus.ASSIGNED);
        return userChallengeRepository.save(userChallenge);
    }

    @Override
    public UserChallenge getOrAssignDailyChallenge(Long userId, Mood mood) {
        // 1. Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // 2. Define today's date range
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();

        // 3. Check if user already has a challenge assigned today
        List<UserChallenge> existingToday = userChallengeRepository
                .findByUser_IdAndStartTimeAfterAndStatus(userId, startOfToday, CompletionStatus.ASSIGNED);

        if (!existingToday.isEmpty()) {
            // Update mood on existing challenge and return
            UserChallenge existing = existingToday.get(0);
            existing.setMood(mood);
            return userChallengeRepository.save(existing);
        }

        // 4. Select a challenge using the shared selection logic
        Challenge selectedChallenge = selectDailyChallenge(user, mood);

        // 5. Assign with mood and save
        UserChallenge userChallenge = new UserChallenge(user, selectedChallenge, CompletionStatus.ASSIGNED);
        userChallenge.setMood(mood);
        return userChallengeRepository.save(userChallenge);
    }

    /**
     * Selects a daily challenge for a user based on mood, culture, and rotation rules.
     */
    private Challenge selectDailyChallenge(User user, Mood mood) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();

        ChallengeCategory yesterdayCategory = getYesterdayCategory(user.getId(), startOfToday);

        EnergyLevel targetEnergyLevel = mapMoodToEnergyLevel(mood);
        List<Challenge> moodMatchingChallenges = challengeRepository
                .findByEnergyLevelAndActiveTrue(targetEnergyLevel);

        Culture userCountry = user.getCountry();
        boolean isCrossCulturalDay = isCrossCulturalDay(user.getId());
        List<Challenge> cultureMatchingChallenges = filterByCulture(
                moodMatchingChallenges, userCountry, isCrossCulturalDay
        );

        List<Challenge> availableChallenges = filterOutCategory(cultureMatchingChallenges, yesterdayCategory);

        if (availableChallenges.isEmpty()) {
            availableChallenges = cultureMatchingChallenges;
        }

        if (availableChallenges.isEmpty()) {
            availableChallenges = moodMatchingChallenges;
        }

        if (availableChallenges.isEmpty()) {
            availableChallenges = challengeRepository.findByActiveTrue();
        }

        if (availableChallenges.isEmpty()) {
            throw new IllegalStateException("No active challenges available");
        }

        Random random = new Random();
        return availableChallenges.get(random.nextInt(availableChallenges.size()));
    }

    /**
     * Maps user's mood to appropriate challenge energy level.
     *
     * Logic:
     * - LOW mood → LOW energy (user needs something achievable, not discouraging)
     * - NEUTRAL mood → MEDIUM energy (balanced, moderate challenge)
     * - HIGH mood → HIGH energy (user has motivation for bigger challenges)
     *
     * @param mood the user's mood
     * @return the corresponding energy level for challenges
     */
    private EnergyLevel mapMoodToEnergyLevel(Mood mood) {
        return switch (mood) {
            case LOW -> EnergyLevel.LOW;
            case NEUTRAL -> EnergyLevel.MEDIUM;
            case HIGH -> EnergyLevel.HIGH;
        };
    }

    /**
     * Gets the category of the most recently assigned challenge for a user before today.
     * Used to avoid repeating the same category on consecutive days.
     *
     * @param userId the ID of the user
     * @param startOfToday today's start time (used as threshold)
     * @return the category of yesterday's challenge, or null if no previous challenge exists
     */
    private ChallengeCategory getYesterdayCategory(Long userId, LocalDateTime startOfToday) {
        List<UserChallenge> previousChallenges = userChallengeRepository
                .findLatestChallengeBeforeDate(userId, startOfToday);

        if (previousChallenges.isEmpty()) {
            return null;  // No previous challenge - no category to avoid
        }

        return previousChallenges.get(0).getChallenge().getCategory();
    }

    /**
     * Filters out challenges from a specific category.
     * If categoryToExclude is null, returns all challenges unchanged.
     *
     * @param challenges the list of challenges to filter
     * @param categoryToExclude the category to exclude, or null to exclude nothing
     * @return filtered list of challenges
     */
    private List<Challenge> filterOutCategory(List<Challenge> challenges, ChallengeCategory categoryToExclude) {
        if (categoryToExclude == null) {
            return challenges;
        }

        final ChallengeCategory toExclude = categoryToExclude;
        return challenges.stream()
                .filter(c -> c.getCategory() != toExclude)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Determines if today should be a cross-cultural challenge day.
     * Every 7th challenge is cross-cultural (configurable).
     *
     * @param userId the ID of the user
     * @return true if today should be a cross-cultural day
     */
    private boolean isCrossCulturalDay(Long userId) {
        // Get user's total assigned challenges
        long totalAssigned = userChallengeRepository.countByUser_Id(userId);

        // Every 7th challenge is cross-cultural (configurable)
        // Days: 1,2,3,4,5,6,7(cross), 8,9,10,11,12,13,14(cross)...
        return totalAssigned > 0 && totalAssigned % 7 == 0;
    }

    /**
     * Filters challenges by culture based on user's country and cross-cultural day.
     * - Normal day: user's country OR GLOBAL
     * - Cross-cultural day: a different country OR GLOBAL
     *
     * @param challenges the list of challenges to filter
     * @param userCountry the user's home country (may be null)
     * @param isCrossCulturalDay whether today is a cross-cultural day
     * @return filtered list of challenges matching the target culture
     */
    private List<Challenge> filterByCulture(
            List<Challenge> challenges,
            Culture userCountry,
            boolean isCrossCulturalDay
    ) {
        Culture targetCulture = getTargetCulture(userCountry, isCrossCulturalDay);

        // Match target culture OR GLOBAL (GLOBAL is always included)
        final Culture culture = targetCulture;

        return challenges.stream()
                .filter(c -> c.getCulture() == culture || c.getCulture() == Culture.GLOBAL)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Determines the target culture based on user's country and cross-cultural day.
     *
     * @param userCountry the user's home country (may be null)
     * @param isCrossCulturalDay whether today is a cross-cultural day
     * @return the target culture for challenge selection
     */
    private Culture getTargetCulture(Culture userCountry, boolean isCrossCulturalDay) {
        if (isCrossCulturalDay) {
            // Pick a different culture for cross-cultural exposure
            if (userCountry == null || userCountry == Culture.INDIA) {
                return Culture.RUSSIA;
            }
            return Culture.INDIA;
        }

        // Normal day: use user's country, default to GLOBAL if not set
        return userCountry != null ? userCountry : Culture.GLOBAL;
    }

    @Override
    @Transactional
    public long clearPendingChallenges(Long userId) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // Delete all challenges with ASSIGNED status for this user
        // COMPLETED challenges are preserved
        long deletedCount = userChallengeRepository.deleteByUser_IdAndStatus(
                userId, CompletionStatus.ASSIGNED
        );

        return deletedCount;
    }

    @Override
    @Transactional
    public UserChallenge startChallenge(Long userId, Long challengeId, Mood mood) {
        // Validate inputs
        if (userId == null || challengeId == null || mood == null) {
            throw new IllegalArgumentException("userId, challengeId, and mood are required");
        }

        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if challenge exists
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found with id: " + challengeId));

        // Check if user already has this challenge assigned for today
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();

        Optional<UserChallenge> existing = userChallengeRepository
                .findByUser_IdAndChallenge_IdAndStartTimeAfterAndStatus(
                        userId,
                        challengeId,
                        startOfToday,
                        CompletionStatus.ASSIGNED
                );

        if (existing.isPresent()) {
            // Idempotent - return existing challenge
            return existing.get();
        }

        // Create new UserChallenge with explicit start time
        UserChallenge userChallenge = new UserChallenge(user, challenge, CompletionStatus.ASSIGNED);
        userChallenge.setMood(mood);
        userChallenge.setStartTime(LocalDateTime.now()); // EXPLICIT START TIMESTAMP

        return userChallengeRepository.save(userChallenge);
    }
}
