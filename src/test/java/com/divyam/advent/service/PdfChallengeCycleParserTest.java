package com.divyam.advent.service;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfChallengeCycleParserTest {

    private final PdfChallengeCycleParser parser = new PdfChallengeCycleParser();

    @Test
    void buildsThirtyDayBalancedCycleFromPdfText() {
        String sampleText = """
                Hard challenges: 1. Cooking 2. Find the nearest blooming tree and take a photo. 3. Fly a kite. 4. Make a zine.
                5. Visit a cafe popular among students. 6. Colorful walk. 7. Make and gift someone a card.
                8. Capture a movie moment photo. 9. Find a statue and look up why it was made. 10. Try local snack.
                11. Practice calligraphy. 12. Write your own limerick. 13. Explore a local market.
                14. Learn one interesting story about your city history. 15. Contact your family.
                16. Make a little bouquet. 17. Watch the sunset from a non-touristy spot. 18. Go to any museum available.
                19. Have a picnic. 20. Take a creative photo framing your university through something.
                21. Cook a dish from your home country and share a pic. 22. Play Crocodile.
                23. Recreate a photo from your childhood. 24. Listen to an album popular in another country.
                25. Try African food you have never tried before. 26. Watch a vampire themed movie.
                27. Compliment at least three people on something non-obvious.
                28. Find three different bird species and photograph them. 29. Make a collage using newspapers.
                30. Leave a nice message for your neighbor. 31. Write a thank you note.
                Chill challenges: 1. Cleaning 2. Find the nearest blooming tree and take a photo. 3. Fly a paper plane into the sky.
                4. Take a photo with today's newspaper. 5. Take a photo of your meal. 6. Find something of your favorite color.
                7. Make and gift someone a simple card. 8. Capture a movie moment photo. 9. Find a statue.
                10. Try local snack. 11. Practice calligraphy. 12. Find a limerick that you like.
                13. Explore a local market. 14. Talk to a local and learn one interesting story about city history.
                15. Contact your family. 16. Make an origami flower. 17. Watch the sunset from a non-touristy spot.
                18. Take a photo of any museum that you find pleasant looking. 19. Drink two liters of water today.
                20. Take a creative photo framing your university through something.
                21. Incorporate your culture in your look. 22. Learn what thumbs up means in other countries.
                23. Walk down a street you have never explored. 24. Listen to a song popular in another country.
                25. Learn the capitals of the biggest countries in Africa. 26. Watch a vampire themed movie.
                27. Compliment someone on something non-obvious. 28. Photograph a bird. 29. Draw a doodle and photograph it.
                30. Leave a small note for your neighbor. 31. Write a thank you note.
                Hi everyone,
                """;

        PdfChallengeCycleParser.ParsedChallengeCycle cycle = parser.parseText(sampleText, "pdf:test");

        assertEquals(30, cycle.days().size());
        assertEquals("Cleaning Reset", cycle.days().get(0).title());
        assertEquals("Cleaning", cycle.days().get(0).description());
        assertEquals("Hard", cycle.days().get(1).difficulty());
        assertEquals("Find the nearest blooming tree and take a photo.", cycle.days().get(1).description());
        assertEquals("Leave a nice message for your neighbor.", cycle.days().get(29).description());
    }

    @Test
    void parsesTheRealAdventCalendarPdf() throws Exception {
        Path pdfPath = Path.of("..", "Advent-calendar.pdf").normalize();
        assertTrue(pdfPath.toFile().exists(), "Expected Advent-calendar.pdf to exist next to the backend");

        PdfChallengeCycleParser.ParsedChallengeCycle cycle = parser.parse(pdfPath);

        assertEquals(30, cycle.days().size());
        assertEquals("Cleaning Reset", cycle.days().get(0).title());
        assertEquals("Easy", cycle.days().get(0).difficulty());
        assertEquals("Blooming Tree Hunt", cycle.days().get(1).title());
        assertEquals("Hard", cycle.days().get(1).difficulty());
    }
}
