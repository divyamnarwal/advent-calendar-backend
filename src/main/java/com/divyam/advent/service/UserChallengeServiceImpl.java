package com.divyam.advent.service;

import com.divyam.advent.dto.UserProgressDto;
import com.divyam.advent.enums.CompletionStatus;
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
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserChallengeServiceImpl implements UserChallengeService {

    private final UserChallengeRepository userChallengeRepository;
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final BadgeService badgeService;
    private final ChallengeCycleSyncService challengeCycleSyncService;
    private final ConcurrentHashMap<PreviewKey, Challenge> previewCache = new ConcurrentHashMap<>();

    private record PreviewKey(Long userId, LocalDate date, Mood mood) {
    }

    @Autowired
    public UserChallengeServiceImpl(
            UserChallengeRepository userChallengeRepository,
            UserRepository userRepository,
            ChallengeRepository challengeRepository,
            BadgeService badgeService,
            ChallengeCycleSyncService challengeCycleSyncService
    ) {
        this.userChallengeRepository = userChallengeRepository;
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
        this.badgeService = badgeService;
        this.challengeCycleSyncService = challengeCycleSyncService;
    }

    @Override
    public UserChallenge joinChallenge(Long userId, Long challengeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found with id: " + challengeId));

        UserChallenge existing = userChallengeRepository
                .findByUser_IdAndChallenge_Id(userId, challengeId)
                .orElse(null);
        if (existing != null) {
            return existing;
        }

        return userChallengeRepository.save(new UserChallenge(user, challenge, CompletionStatus.ASSIGNED));
    }

    @Override
    public List<UserChallenge> getUserChallenges(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return userChallengeRepository.findByUser_Id(userId);
    }

    @Override
    public List<UserChallenge> getChallengeParticipants(Long challengeId) {
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
        UserChallenge userChallenge = getUserChallengeById(id);

        if (userChallenge.getStatus() != CompletionStatus.ASSIGNED) {
            throw new IllegalStateException(
                    "Cannot complete challenge with status: " + userChallenge.getStatus() +
                            ". Only ASSIGNED challenges can be marked as COMPLETED."
            );
        }

        userChallenge.setStatus(CompletionStatus.COMPLETED);
        userChallenge.setCompletionTime(LocalDateTime.now());

        UserChallenge saved = userChallengeRepository.save(userChallenge);
        badgeService.evaluateAndAssignBadges(saved.getUser());
        return saved;
    }

    @Override
    public UserChallenge updateStatus(Long id, CompletionStatus status) {
        UserChallenge userChallenge = getUserChallengeById(id);
        userChallenge.setStatus(status);

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
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return userChallengeRepository.findByUser_IdAndStatus(userId, status);
    }

    @Override
    public UserProgressDto getUserProgress(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        long totalAssigned = userChallengeRepository.countByUser_Id(userId);
        long totalCompleted = userChallengeRepository.countByUser_IdAndStatus(
                userId, CompletionStatus.COMPLETED
        );

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

        Challenge selected = selectDailyChallenge(user);
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
            expected = selectDailyChallenge(user);
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        List<UserChallenge> existingToday = userChallengeRepository
                .findByUser_IdAndStartTimeAfterAndStatus(userId, startOfToday, CompletionStatus.ASSIGNED);

        if (!existingToday.isEmpty()) {
            return existingToday.get(0);
        }

        Challenge selectedChallenge = selectDailyChallenge(user);
        return userChallengeRepository.save(new UserChallenge(user, selectedChallenge, CompletionStatus.ASSIGNED));
    }

    @Override
    public UserChallenge getOrAssignDailyChallenge(Long userId, Mood mood) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        List<UserChallenge> existingToday = userChallengeRepository
                .findByUser_IdAndStartTimeAfterAndStatus(userId, startOfToday, CompletionStatus.ASSIGNED);

        if (!existingToday.isEmpty()) {
            UserChallenge existing = existingToday.get(0);
            existing.setMood(mood);
            return userChallengeRepository.save(existing);
        }

        Challenge selectedChallenge = selectDailyChallenge(user);
        UserChallenge userChallenge = new UserChallenge(user, selectedChallenge, CompletionStatus.ASSIGNED);
        userChallenge.setMood(mood);
        return userChallengeRepository.save(userChallenge);
    }

    private Challenge selectDailyChallenge(User user) {
        String currentSourceVersion = challengeCycleSyncService.getCurrentSourceVersion();
        List<Challenge> cycleChallenges = challengeRepository
                .findBySourceVersionAndActiveTrueOrderByCycleDayAsc(currentSourceVersion);

        if (cycleChallenges.isEmpty()) {
            throw new IllegalStateException("No active PDF challenge cycle is available");
        }

        long assignedInCurrentCycle = userChallengeRepository
                .countByUserIdAndChallengeSourceVersion(user.getId(), currentSourceVersion);
        int nextCycleDay = (int) (assignedInCurrentCycle % cycleChallenges.size()) + 1;

        return challengeRepository.findBySourceVersionAndCycleDayAndActiveTrue(currentSourceVersion, nextCycleDay)
                .orElseThrow(() -> new IllegalStateException(
                        "Challenge day " + nextCycleDay + " is missing from source " + currentSourceVersion
                ));
    }

    @Override
    @Transactional
    public long clearPendingChallenges(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return userChallengeRepository.deleteByUser_IdAndStatus(userId, CompletionStatus.ASSIGNED);
    }

    @Override
    @Transactional
    public UserChallenge startChallenge(Long userId, Long challengeId, Mood mood) {
        if (userId == null || challengeId == null || mood == null) {
            throw new IllegalArgumentException("userId, challengeId, and mood are required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found with id: " + challengeId));

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        Optional<UserChallenge> existing = userChallengeRepository
                .findByUser_IdAndChallenge_IdAndStartTimeAfterAndStatus(
                        userId,
                        challengeId,
                        startOfToday,
                        CompletionStatus.ASSIGNED
                );

        if (existing.isPresent()) {
            return existing.get();
        }

        UserChallenge userChallenge = new UserChallenge(user, challenge, CompletionStatus.ASSIGNED);
        userChallenge.setMood(mood);
        userChallenge.setStartTime(LocalDateTime.now());

        return userChallengeRepository.save(userChallenge);
    }
}
