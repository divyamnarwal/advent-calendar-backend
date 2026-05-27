package com.divyam.advent.service;

/**
 * Faceit-style 1–10 levels derived from ELO. Thresholds are tuned to the app's ELO scale
 * (roughly 15–40 ELO per completed quest).
 */
public final class EloLevels {

    public static final int MAX_LEVEL = 10;

    /** Minimum ELO for each level; index 0 = level 1 … index 9 = level 10. */
    private static final long[] MIN_ELO = {0, 100, 250, 450, 700, 1000, 1350, 1750, 2200, 2700};

    public record LevelInfo(int level, long currentLevelElo, Long nextLevelElo) {
    }

    private EloLevels() {
    }

    public static int levelFor(long elo) {
        int level = 1;
        for (int i = 0; i < MIN_ELO.length; i++) {
            if (elo >= MIN_ELO[i]) {
                level = i + 1;
            }
        }
        return level;
    }

    public static LevelInfo infoFor(long elo) {
        int level = levelFor(elo);
        long currentLevelElo = MIN_ELO[level - 1];
        Long nextLevelElo = level < MAX_LEVEL ? MIN_ELO[level] : null;
        return new LevelInfo(level, currentLevelElo, nextLevelElo);
    }
}
