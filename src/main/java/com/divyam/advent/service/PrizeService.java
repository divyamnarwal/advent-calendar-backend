package com.divyam.advent.service;

import com.divyam.advent.dto.PrizeDto;
import com.divyam.advent.dto.PrizeLeaderboardEntryDto;
import com.divyam.advent.dto.PrizeRequestDto;

import java.util.List;

public interface PrizeService {

    List<PrizeDto> listAll();

    List<PrizeDto> listActive();

    List<PrizeDto> listAwardedToUser(Long userId);

    PrizeDto create(PrizeRequestDto request);

    PrizeDto update(Long id, PrizeRequestDto request);

    void delete(Long id);

    List<PrizeLeaderboardEntryDto> leaderboard(Long prizeId, int limit);

    PrizeDto award(Long prizeId, Long userId);

    PrizeDto unaward(Long prizeId);
}
