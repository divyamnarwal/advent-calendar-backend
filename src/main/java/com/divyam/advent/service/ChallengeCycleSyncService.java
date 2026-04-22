package com.divyam.advent.service;

import com.divyam.advent.enums.ChallengeCategory;
import com.divyam.advent.enums.EnergyLevel;
import com.divyam.advent.model.Challenge;
import com.divyam.advent.repository.ChallengeRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ChallengeCycleSyncService {

    private static final Logger log = LoggerFactory.getLogger(ChallengeCycleSyncService.class);

    private final PdfChallengeCycleParser pdfChallengeCycleParser;
    private final ChallengeRepository challengeRepository;
    private final String configuredPdfPath;

    public ChallengeCycleSyncService(
            PdfChallengeCycleParser pdfChallengeCycleParser,
            ChallengeRepository challengeRepository,
            @Value("${challenge.cycle.pdf-path:../Advent-calendar.pdf}") String configuredPdfPath
    ) {
        this.pdfChallengeCycleParser = pdfChallengeCycleParser;
        this.challengeRepository = challengeRepository;
        this.configuredPdfPath = configuredPdfPath;
    }

    @PostConstruct
    @Transactional
    public void syncCycleFromPdf() {
        Path pdfPath = resolvePdfPath();
        if (pdfPath == null) {
            log.warn("Challenge PDF not found. Skipping cycle sync for path {}", configuredPdfPath);
            return;
        }

        try {
            PdfChallengeCycleParser.ParsedChallengeCycle parsedCycle = pdfChallengeCycleParser.parse(pdfPath);
            List<Challenge> existingActiveCycle = challengeRepository
                    .findBySourceVersionAndActiveTrueOrderByCycleDayAsc(parsedCycle.sourceVersion());

            if (existingActiveCycle.size() == parsedCycle.days().size()) {
                log.info("Challenge cycle already synced from {}", pdfPath);
                return;
            }

            challengeRepository.deactivateChallengesOutsideSourceVersion(parsedCycle.sourceVersion());
            persistCycle(parsedCycle);
            log.info("Synced {} challenge days from {}", parsedCycle.days().size(), pdfPath);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse challenge PDF: " + pdfPath, exception);
        }
    }

    public List<Challenge> getCurrentCycleChallenges() {
        return challengeRepository.findBySourceVersionAndActiveTrueOrderByCycleDayAsc(getCurrentSourceVersion());
    }

    public String getCurrentSourceVersion() {
        return challengeRepository.findCurrentSourceVersion()
                .orElseThrow(() -> new IllegalStateException("No active challenge cycle is available"));
    }

    private void persistCycle(PdfChallengeCycleParser.ParsedChallengeCycle parsedCycle) {
        for (PdfChallengeCycleParser.ParsedChallengeDay day : parsedCycle.days()) {
            Challenge challenge = new Challenge();
            challenge.setTitle(day.title());
            challenge.setDescription(day.description());
            challenge.setCategory(ChallengeCategory.WILDCARD);
            challenge.setEnergyLevel("Hard".equalsIgnoreCase(day.difficulty()) ? EnergyLevel.HIGH : EnergyLevel.LOW);
            challenge.setActive(true);
            challenge.setCycleDay(day.dayNumber());
            challenge.setSourceVersion(parsedCycle.sourceVersion());
            challengeRepository.save(challenge);
        }
    }

    private Path resolvePdfPath() {
        Path configuredPath = Paths.get(configuredPdfPath).normalize();
        if (Files.exists(configuredPath)) {
            return configuredPath;
        }

        Path localPath = Paths.get("Advent-calendar.pdf").normalize();
        if (Files.exists(localPath)) {
            return localPath;
        }

        Path parentPath = Paths.get("..", "Advent-calendar.pdf").normalize();
        if (Files.exists(parentPath)) {
            return parentPath;
        }

        return null;
    }
}
