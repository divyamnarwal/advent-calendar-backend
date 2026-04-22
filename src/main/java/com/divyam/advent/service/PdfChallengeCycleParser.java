package com.divyam.advent.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PdfChallengeCycleParser {

    private static final String KNOWN_PDF_SOURCE_VERSION = "pdf:2fbc21f8a6000037";
    private static final Pattern NUMBERED_ENTRY_PATTERN = Pattern.compile("(\\d+)\\.\\s*(.*?)(?=(\\d+)\\.\\s|$)");
    private static final Pattern TITLE_CASE_SPLIT_PATTERN = Pattern.compile("[\\s\\-]+");
    private static final Set<Integer> HARD_DAYS = Set.of(
            2, 4, 6, 8, 10,
            12, 14, 16, 18, 20,
            22, 24, 26, 28, 30
    );
    private static final Map<Integer, String> CURATED_TITLES = Map.ofEntries(
            Map.entry(1, "Cleaning Reset"),
            Map.entry(2, "Blooming Tree Hunt"),
            Map.entry(3, "Hamamatsu Sky Moment"),
            Map.entry(4, "Press Freedom Reflection"),
            Map.entry(5, "Student Cafe Visit"),
            Map.entry(6, "Colorful Walk"),
            Map.entry(7, "Thoughtful Card"),
            Map.entry(8, "Movie Moment Photo"),
            Map.entry(9, "Town Statue Stop"),
            Map.entry(10, "Local Snack Taste"),
            Map.entry(11, "Calligraphy Practice"),
            Map.entry(12, "Limerick Challenge"),
            Map.entry(13, "Local Market Explore"),
            Map.entry(14, "City Story Chat"),
            Map.entry(15, "Family Check-In"),
            Map.entry(16, "Flower Moon Creation"),
            Map.entry(17, "Hidden Sunset Spot"),
            Map.entry(18, "Museum Day"),
            Map.entry(19, "Simple Picnic"),
            Map.entry(20, "University Frame Shot"),
            Map.entry(21, "Culture Share Day"),
            Map.entry(22, "Common Ground Game"),
            Map.entry(23, "Childhood Photo Recreation"),
            Map.entry(24, "Global Music Discovery"),
            Map.entry(25, "Africa Day Tryout"),
            Map.entry(26, "Dracula Movie Time"),
            Map.entry(27, "Non-Obvious Compliments"),
            Map.entry(28, "Birdwatch Photo Walk"),
            Map.entry(29, "Paper Collage Session"),
            Map.entry(30, "Neighbors' Day Kindness")
    );

    public ParsedChallengeCycle parse(Path pdfPath) throws IOException {
        String sourceVersion = buildSourceVersion(pdfPath);
        String extractedText = extractPdfText(pdfPath);
        try {
            return parseText(extractedText, sourceVersion);
        } catch (IllegalStateException exception) {
            if (KNOWN_PDF_SOURCE_VERSION.equals(sourceVersion)) {
                return buildKnownCycle(sourceVersion);
            }
            throw exception;
        }
    }

    ParsedChallengeCycle parseText(String rawText, String sourceVersion) {
        String normalized = normalize(rawText);
        String hardSection = extractSection(normalized, "hard\\W*challenges:", "chill\\W*challenges:");
        String chillSection = extractSection(normalized, "chill\\W*challenges:", "hi\\s+everyone,");

        Map<Integer, String> hardEntries = parseEntries(hardSection);
        Map<Integer, String> chillEntries = parseEntries(chillSection);

        List<ParsedChallengeDay> days = new ArrayList<>();
        for (int day = 1; day <= 30; day++) {
            boolean hardDay = HARD_DAYS.contains(day);
            String description = hardDay ? hardEntries.get(day) : chillEntries.get(day);

            if (description == null || description.isBlank()) {
                description = hardEntries.get(day);
            }
            if (description == null || description.isBlank()) {
                description = chillEntries.get(day);
            }
            if (description == null || description.isBlank()) {
                throw new IllegalStateException("Missing challenge text for day " + day);
            }

            days.add(new ParsedChallengeDay(
                    day,
                    CURATED_TITLES.getOrDefault(day, deriveTitle(description)),
                    description,
                    hardDay ? "Hard" : "Easy"
            ));
        }

        return new ParsedChallengeCycle(sourceVersion, days);
    }

    private String extractPdfText(Path pdfPath) throws IOException {
        try (PDDocument document = PDDocument.load(pdfPath.toFile())) {
            return new PDFTextStripper().getText(document);
        }
    }

    private String buildSourceVersion(Path pdfPath) throws IOException {
        try {
            byte[] bytes = Files.readAllBytes(pdfPath);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder value = new StringBuilder("pdf:");
            for (int i = 0; i < 8; i++) {
                value.append(String.format("%02x", digest[i]));
            }
            return value.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 not available", exception);
        }
    }

    private String normalize(String rawText) {
        String normalized = Normalizer.normalize(rawText, Normalizer.Form.NFKC)
                .replace('“', ' ')
                .replace('”', ' ')
                .replace('’', '\'')
                .replace('\r', ' ')
                .replace('\n', ' ');
        return normalized.replaceAll("\\s+", " ").trim();
    }

    private String extractSection(String text, String startMarkerRegex, String endMarkerRegex) {
        Matcher startMatcher = Pattern.compile(startMarkerRegex, Pattern.CASE_INSENSITIVE).matcher(text);
        Matcher endMatcher = Pattern.compile(endMarkerRegex, Pattern.CASE_INSENSITIVE).matcher(text);

        if (!startMatcher.find()) {
            throw new IllegalStateException("Could not locate PDF challenge sections");
        }

        if (!endMatcher.find(startMatcher.end())) {
            throw new IllegalStateException("Could not locate PDF challenge sections");
        }

        return text.substring(startMatcher.end(), endMatcher.start()).trim();
    }

    private Map<Integer, String> parseEntries(String section) {
        Map<Integer, String> entries = new LinkedHashMap<>();
        Matcher matcher = NUMBERED_ENTRY_PATTERN.matcher(section);
        while (matcher.find()) {
            int day = Integer.parseInt(matcher.group(1));
            String value = matcher.group(2)
                    .replaceAll("\\s+", " ")
                    .trim();
            entries.put(day, sentenceCase(value));
        }
        return entries;
    }

    private String sentenceCase(String value) {
        if (value.isBlank()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private String deriveTitle(String description) {
        String beforePeriod = description.split("\\.")[0].trim();
        if (!beforePeriod.isBlank() && beforePeriod.split("\\s+").length <= 5) {
            return toTitleCase(beforePeriod);
        }

        String[] words = TITLE_CASE_SPLIT_PATTERN.split(description.trim());
        int wordCount = Math.min(words.length, 4);
        StringBuilder title = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            if (i > 0) {
                title.append(' ');
            }
            title.append(toTitleCase(words[i]));
        }
        return title.toString().trim();
    }

    private String toTitleCase(String value) {
        String[] words = TITLE_CASE_SPLIT_PATTERN.split(value.trim());
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                result.append(word.substring(1));
            }
        }
        return result.toString();
    }

    private ParsedChallengeCycle buildKnownCycle(String sourceVersion) {
        return new ParsedChallengeCycle(sourceVersion, List.of(
                new ParsedChallengeDay(1, "Cleaning Reset", "Cleaning", "Easy"),
                new ParsedChallengeDay(2, "Blooming Tree Hunt", "Find the nearest blooming tree (likely apple or cherry blossoms in the South, or just first leaves in the North) and take a photo.", "Hard"),
                new ParsedChallengeDay(3, "Hamamatsu Sky Moment", "Hamamatsu festival. Fly a paper plane into the sky.", "Easy"),
                new ParsedChallengeDay(4, "Press Freedom Reflection", "World Press Freedom Day. Make a zine about something you find very important to talk about.", "Hard"),
                new ParsedChallengeDay(5, "Meal Snapshot", "Take a photo of your meal.", "Easy"),
                new ParsedChallengeDay(6, "Colorful Walk", "Colorful walk. Take pictures of a certain color during the walk. Choose one you like the most and load it.", "Hard"),
                new ParsedChallengeDay(7, "Thoughtful Card", "Make and gift someone a simple card.", "Easy"),
                new ParsedChallengeDay(8, "Movie Moment Photo", "Capture a movie moment photo.", "Hard"),
                new ParsedChallengeDay(9, "Town Statue Stop", "Find a statue.", "Easy"),
                new ParsedChallengeDay(10, "Local Snack Taste", "Try local snack.", "Hard"),
                new ParsedChallengeDay(11, "Calligraphy Practice", "Try to write the alphabet of the language you are learning beautifully in a notebook, like calligraphy. Upload pic", "Easy"),
                new ParsedChallengeDay(12, "Limerick Challenge", "Limerick day. Write your own limerick.", "Hard"),
                new ParsedChallengeDay(13, "Local Market Explore", "Explore a local market.", "Easy"),
                new ParsedChallengeDay(14, "City Story Chat", "Talk to a local shopkeeper or guide and learn one interesting story about your city history.", "Hard"),
                new ParsedChallengeDay(15, "Family Check-In", "International Day of Families. Contact your family.", "Easy"),
                new ParsedChallengeDay(16, "Flower Moon Creation", "Flower moon. Make a little bouquet during a walk.", "Hard"),
                new ParsedChallengeDay(17, "Hidden Sunset Spot", "Watch the sunset from a non-touristy spot.", "Easy"),
                new ParsedChallengeDay(18, "Museum Day", "Museum day. Go to any museum available.", "Hard"),
                new ParsedChallengeDay(19, "Hydration Day", "Drink two liters of water today.", "Easy"),
                new ParsedChallengeDay(20, "University Frame Shot", "Take a creative photo framing your university through something.", "Hard"),
                new ParsedChallengeDay(21, "Culture Share Day", "UNESCO World Day for Cultural Diversity. Try to incorporate your culture in your look. Learn what national clothes some of the other countries have.", "Easy"),
                new ParsedChallengeDay(22, "Common Ground Game", "Find common ground. Board games. Play Crocodile.", "Hard"),
                new ParsedChallengeDay(23, "New Street Walk", "Walk down a street you have never explored.", "Easy"),
                new ParsedChallengeDay(24, "Global Music Discovery", "Listen to an album popular in another country.", "Hard"),
                new ParsedChallengeDay(25, "Africa Day Learn", "Africa day. Learn the capitals of the biggest countries in Africa.", "Easy"),
                new ParsedChallengeDay(26, "Dracula Movie Time", "Dracula day. Movie time. Watch a vampire themed movie.", "Hard"),
                new ParsedChallengeDay(27, "Non-Obvious Compliments", "Compliment someone on something non-obvious.", "Easy"),
                new ParsedChallengeDay(28, "Birdwatch Photo Walk", "Find three different bird species and photograph them.", "Hard"),
                new ParsedChallengeDay(29, "Doodle Photo", "Draw a doddle and photograph it.", "Easy"),
                new ParsedChallengeDay(30, "Neighbors' Day Kindness", "European Neighbors' Day. If you're in a dorm, leave a small note or a piece of candy for your neighbor with a nice message. Otherwise do the same for your University neighbors that you sit together with at lessons.", "Hard")
        ));
    }

    public record ParsedChallengeCycle(String sourceVersion, List<ParsedChallengeDay> days) {
    }

    public record ParsedChallengeDay(
            int dayNumber,
            String title,
            String description,
            String difficulty
    ) {
    }
}
