package com.mwtek.shootout.cli;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.mwtek.shootout.game.ShootoutLimits;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import org.junit.jupiter.api.Test;

class CowboyCountPromptTest {
    @Test
    void keepsAskingUntilTheCowboyCountIsValid() throws Exception {
        final String enteredValues = "-1\nzero\n"
                + (ShootoutLimits.MAX_BATCH_COWBOYS + 1) + "\n20\n";
        final ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();

        final int numberOfCowboys = CowboyCountPrompt.read(
                new BufferedReader(new StringReader(enteredValues)),
                new PrintStream(outputBytes, true, UTF_8)).orElseThrow();

        final String consoleOutput = outputBytes.toString(UTF_8);
        assertEquals(20, numberOfCowboys);
        assertTrue(consoleOutput.contains("Cowboy count must be positive"));
        assertTrue(consoleOutput.contains("enter a whole number"));
        assertTrue(consoleOutput.contains(
                "Batch mode supports at most 1000000 cowboys"));
        assertTrue(consoleOutput.contains("Number of cowboys"));
        assertFalse(consoleOutput.contains("Number of cowboys: 1-1000000"));
        assertTrue(consoleOutput.contains("Press Enter for 5 or Q to quit."));
        assertTrue(consoleOutput.contains(
                "Counts above 10000 automatically use compact batch mode."));
    }

    @Test
    void acceptsACowboyCountThatRequiresCompactBatchMode() throws Exception {
        final int numberOfCowboys = CowboyCountPrompt.read(
                new BufferedReader(new StringReader("500111\n")),
                new PrintStream(new ByteArrayOutputStream(), true, UTF_8)).orElseThrow();

        assertEquals(500_111, numberOfCowboys);
    }

    @Test
    void usesFiveCowboysWhenEnterIsPressed() throws Exception {
        final int numberOfCowboys = CowboyCountPrompt.read(
                new BufferedReader(new StringReader("\n")),
                new PrintStream(new ByteArrayOutputStream(), true, UTF_8)).orElseThrow();

        assertEquals(5, numberOfCowboys);
    }

    @Test
    void quitsWhenQIsEntered() throws Exception {
        assertTrue(CowboyCountPrompt.read(
                new BufferedReader(new StringReader("Q\n")),
                new PrintStream(new ByteArrayOutputStream(), true, UTF_8)).isEmpty());
    }
}