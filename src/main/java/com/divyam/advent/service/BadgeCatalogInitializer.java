package com.divyam.advent.service;

import com.divyam.advent.model.Badge;
import com.divyam.advent.repository.BadgeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BadgeCatalogInitializer {

    private final BadgeRepository badgeRepository;

    public BadgeCatalogInitializer(BadgeRepository badgeRepository) {
        this.badgeRepository = badgeRepository;
    }

    @PostConstruct
    @Transactional
    public void initializeBadges() {
        seedBadge(
                "STREAK_3_DAYS",
                "3 Day Streak",
                "Complete challenges for 3 consecutive days.",
                "flame",
                "STREAK_DAYS:3"
        );
        seedBadge(
                "STREAK_7_DAYS",
                "7 Day Streak",
                "Complete challenges for 7 consecutive days.",
                "bolt",
                "STREAK_DAYS:7"
        );
        seedBadge(
                "FIRST_CHALLENGE_COMPLETED",
                "First Challenge Completed",
                "Complete your first challenge.",
                "sparkles",
                "COMPLETED_CHALLENGES:1"
        );
        seedBadge(
                "CHALLENGES_10_COMPLETED",
                "10 Challenges Completed",
                "Complete 10 total challenges.",
                "trophy",
                "COMPLETED_CHALLENGES:10"
        );
        seedBadge(
                "CONSISTENCY_30_DAYS",
                "30 Day Consistency Badge",
                "Maintain a 30 day streak.",
                "crown",
                "STREAK_DAYS:30"
        );
    }

    private void seedBadge(String id, String title, String description, String icon, String criteria) {
        if (badgeRepository.existsById(id)) {
            return;
        }
        badgeRepository.save(new Badge(id, title, description, icon, criteria));
    }
}
