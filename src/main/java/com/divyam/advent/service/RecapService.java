package com.divyam.advent.service;

import com.divyam.advent.dto.MonthlyRecapResponseDto;

import java.time.YearMonth;

public interface RecapService {
    MonthlyRecapResponseDto getMonthlyRecap(Long userId, YearMonth month);
}
