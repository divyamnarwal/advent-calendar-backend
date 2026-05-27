package com.divyam.advent.service.impl;

import com.divyam.advent.dto.PrizeDto;
import com.divyam.advent.dto.PrizeLeaderboardEntryDto;
import com.divyam.advent.dto.PrizeRequestDto;
import com.divyam.advent.enums.CompletionStatus;
import com.divyam.advent.enums.PrizeCriteria;
import com.divyam.advent.exception.ResourceNotFoundException;
import com.divyam.advent.model.Challenge;
import com.divyam.advent.model.Prize;
import com.divyam.advent.model.User;
import com.divyam.advent.model.UserChallenge;
import com.divyam.advent.repository.ChallengeRepository;
import com.divyam.advent.repository.PrizeRepository;
import com.divyam.advent.repository.UserChallengeRepository;
import com.divyam.advent.repository.UserRepository;
import com.divyam.advent.service.PrizeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Service
public class PrizeServiceImpl implements PrizeService {

    private final PrizeRepository prizeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;

    public PrizeServiceImpl(
            PrizeRepository prizeRepository,
            UserChallengeRepository userChallengeRepository,
            ChallengeRepository challengeRepository,
            UserRepository userRepository
    ) {
        this.prizeRepository = prizeRepository;
        this.userChallengeRepository = userChallengeRepository;
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
    }

    // --- queries ------------------------------------------------------------

    @Override
    public List<PrizeDto> listAll() {
        return prizeRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
    }

    @Override
    public List<PrizeDto> listActive() {
        return prizeRepository.findByActiveTrueOrderByCreatedAtDesc().stream().map(this::toDto).toList();
    }

    @Override
    public List<PrizeDto> listAwardedToUser(Long userId) {
        return prizeRepository.findByAwardedUserIdOrderByAwardedAtDesc(userId).stream()
                .map(this::toDto).toList();
    }

    // --- mutations ----------------------------------------------------------

    @Override
    @Transactional
    public PrizeDto create(PrizeRequestDto request) {
        Prize prize = new Prize();
        applyRequest(prize, request);
        prize.setCreatedAt(LocalDateTime.now());
        return toDto(prizeRepository.save(prize));
    }

    @Override
    @Transactional
    public PrizeDto update(Long id, PrizeRequestDto request) {
        Prize prize = getEntity(id);
        applyRequest(prize, request);
        return toDto(prizeRepository.save(prize));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!prizeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Prize not found with id: " + id);
        }
        prizeRepository.deleteById(id);
    }

    @Override
    @Transactional
    public PrizeDto award(Long prizeId, Long userId) {
        Prize prize = getEntity(prizeId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        prize.setAwardedUserId(user.getId());
        prize.setAwardedAt(LocalDateTime.now());
        return toDto(prizeRepository.save(prize));
    }

    @Override
    @Transactional
    public PrizeDto unaward(Long prizeId) {
        Prize prize = getEntity(prizeId);
        prize.setAwardedUserId(null);
        prize.setAwardedAt(null);
        return toDto(prizeRepository.save(prize));
    }

    // --- leaderboard --------------------------------------------------------

    @Override
    public List<PrizeLeaderboardEntryDto> leaderboard(Long prizeId, int limit) {
        Prize prize = getEntity(prizeId);
        YearMonth period = resolvePeriod(prize.getPeriodMonth());
        LocalDateTime start = period.atDay(1).atStartOfDay();
        LocalDateTime end = period.atEndOfMonth().atTime(LocalTime.MAX);
        int cap = limit <= 0 ? 20 : Math.min(limit, 100);

        List<PrizeLeaderboardEntryDto> ranked = switch (prize.getCriteria()) {
            case LONGEST_STREAK -> longestStreakBoard(start, end);
            case MOST_COMPLETED -> mostCompletedBoard(start, end);
            case FASTEST_CHALLENGE -> fastestChallengeBoard(prize.getTargetChallengeId(), start, end);
        };
        return ranked.stream().limit(cap).toList();
    }

    private List<PrizeLeaderboardEntryDto> longestStreakBoard(LocalDateTime start, LocalDateTime end) {
        Map<User, TreeSet<LocalDate>> daysByUser = new LinkedHashMap<>();
        for (UserChallenge uc : completedInRange(start, end)) {
            if (uc.getUser() == null || uc.getCompletionTime() == null) {
                continue;
            }
            daysByUser.computeIfAbsent(uc.getUser(), u -> new TreeSet<>())
                    .add(uc.getCompletionTime().toLocalDate());
        }
        List<Object[]> scored = new ArrayList<>();
        daysByUser.forEach((user, days) -> scored.add(new Object[]{user, longestRun(days)}));
        scored.sort(Comparator.comparingLong(o -> -((long) o[1])));
        return toBoard(scored);
    }

    private List<PrizeLeaderboardEntryDto> mostCompletedBoard(LocalDateTime start, LocalDateTime end) {
        Map<User, Long> countByUser = new LinkedHashMap<>();
        for (UserChallenge uc : completedInRange(start, end)) {
            if (uc.getUser() == null) {
                continue;
            }
            countByUser.merge(uc.getUser(), 1L, Long::sum);
        }
        List<Object[]> scored = new ArrayList<>();
        countByUser.forEach((user, count) -> scored.add(new Object[]{user, count}));
        scored.sort(Comparator.comparingLong(o -> -((long) o[1])));
        return toBoard(scored);
    }

    private List<PrizeLeaderboardEntryDto> fastestChallengeBoard(Long challengeId, LocalDateTime start, LocalDateTime end) {
        if (challengeId == null) {
            return List.of();
        }
        List<Object[]> scored = new ArrayList<>();
        for (UserChallenge uc : userChallengeRepository.findByChallenge_Id(challengeId)) {
            if (uc.getStatus() != CompletionStatus.COMPLETED || uc.getUser() == null) {
                continue;
            }
            LocalDateTime startTime = uc.getStartTime();
            LocalDateTime completionTime = uc.getCompletionTime();
            if (startTime == null || completionTime == null) {
                continue;
            }
            if (completionTime.isBefore(start) || completionTime.isAfter(end)) {
                continue;
            }
            long seconds = Duration.between(startTime, completionTime).getSeconds();
            if (seconds < 0) {
                continue;
            }
            scored.add(new Object[]{uc.getUser(), seconds});
        }
        // ascending: fastest (smallest duration) first
        scored.sort(Comparator.comparingLong(o -> (long) o[1]));
        return toBoard(scored);
    }

    private List<UserChallenge> completedInRange(LocalDateTime start, LocalDateTime end) {
        return userChallengeRepository.findByStatusAndCompletionTimeBetween(
                CompletionStatus.COMPLETED, start, end);
    }

    private List<PrizeLeaderboardEntryDto> toBoard(List<Object[]> scored) {
        List<PrizeLeaderboardEntryDto> board = new ArrayList<>();
        int rank = 1;
        for (Object[] row : scored) {
            User user = (User) row[0];
            long metric = (long) row[1];
            board.add(new PrizeLeaderboardEntryDto(
                    rank++, user.getId(), user.getName(), user.getEmail(), metric));
        }
        return board;
    }

    private static long longestRun(TreeSet<LocalDate> days) {
        long best = 0;
        long current = 0;
        LocalDate previous = null;
        for (LocalDate day : days) {
            if (previous != null && day.equals(previous.plusDays(1))) {
                current++;
            } else {
                current = 1;
            }
            best = Math.max(best, current);
            previous = day;
        }
        return best;
    }

    // --- helpers ------------------------------------------------------------

    private Prize getEntity(Long id) {
        return prizeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prize not found with id: " + id));
    }

    private void applyRequest(Prize prize, PrizeRequestDto request) {
        if (request == null || request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("Prize title is required");
        }
        PrizeCriteria criteria = parseCriteria(request.criteria());
        Long targetChallengeId = request.targetChallengeId();
        if (criteria == PrizeCriteria.FASTEST_CHALLENGE) {
            if (targetChallengeId == null) {
                throw new IllegalArgumentException("FASTEST_CHALLENGE requires a target challenge");
            }
            if (!challengeRepository.existsById(targetChallengeId)) {
                throw new ResourceNotFoundException("Challenge not found with id: " + targetChallengeId);
            }
        } else {
            targetChallengeId = null; // ignore for non-challenge criteria
        }

        prize.setTitle(request.title().trim());
        prize.setDescription(request.description());
        prize.setCriteria(criteria);
        prize.setTargetChallengeId(targetChallengeId);
        prize.setPeriodMonth(normalizePeriod(request.periodMonth()));
        prize.setActive(request.active() == null || request.active());
    }

    private static PrizeCriteria parseCriteria(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Prize criteria is required");
        }
        try {
            return PrizeCriteria.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown prize criteria: " + value);
        }
    }

    /** Validates "YYYY-MM" if present; null/blank means current month / ongoing. */
    private static String normalizePeriod(String periodMonth) {
        if (periodMonth == null || periodMonth.isBlank()) {
            return null;
        }
        try {
            return YearMonth.parse(periodMonth.trim()).toString();
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("periodMonth must be in YYYY-MM format");
        }
    }

    private static YearMonth resolvePeriod(String periodMonth) {
        if (periodMonth == null || periodMonth.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(periodMonth.trim());
        } catch (DateTimeParseException ex) {
            return YearMonth.now();
        }
    }

    private PrizeDto toDto(Prize prize) {
        String challengeTitle = null;
        if (prize.getTargetChallengeId() != null) {
            challengeTitle = challengeRepository.findById(prize.getTargetChallengeId())
                    .map(Challenge::getTitle)
                    .orElse(null);
        }
        String awardedUserName = null;
        if (prize.getAwardedUserId() != null) {
            awardedUserName = userRepository.findById(prize.getAwardedUserId())
                    .map(User::getName)
                    .orElse(null);
        }
        return new PrizeDto(
                prize.getId(),
                prize.getTitle(),
                prize.getDescription(),
                prize.getCriteria() != null ? prize.getCriteria().name() : null,
                prize.getTargetChallengeId(),
                challengeTitle,
                prize.getPeriodMonth(),
                prize.isActive(),
                prize.getAwardedUserId(),
                awardedUserName,
                prize.getAwardedAt(),
                prize.getCreatedAt()
        );
    }
}
