package com.mwtek.shootout.cli;

import com.mwtek.shootout.game.ShootoutLimits;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Objects;
import java.util.OptionalInt;

/** Reads and validates the cowboy count for the next interactive game. */
public final class CowboyCountPrompt {
    private static final int DEFAULT_NUMBER_OF_COWBOYS = 5;
    private static final String PROMPT_HEADER_FORMAT = "Number of cowboys%nPress Enter for %d or Q to quit.%n";
    private static final String AUTOMATIC_BATCH_HINT_FORMAT = "Counts above %d automatically use compact batch mode.%n";
    private static final String CHOICE_PROMPT = "Your choice: ";
    private static final String INVALID_COUNT_FORMAT = "Invalid cowboy count: %s%n";
    private static final String WHOLE_NUMBER_REQUIRED = "enter a whole number";
    private static final String QUIT_COMMAND = "q";

    private CowboyCountPrompt() {
    }

    public static OptionalInt read(BufferedReader input, PrintStream output) throws IOException {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(output, "output");

        while (true) {
            output.printf(PROMPT_HEADER_FORMAT, DEFAULT_NUMBER_OF_COWBOYS);
            output.printf(AUTOMATIC_BATCH_HINT_FORMAT, ShootoutLimits.MAX_DETAILED_COWBOYS);
            output.print(CHOICE_PROMPT);
            output.flush();
            final String enteredValue = input.readLine();
            if (enteredValue == null || QUIT_COMMAND.equalsIgnoreCase(enteredValue.trim())) {
                return OptionalInt.empty();
            }
            if (enteredValue.isBlank()) {
                return OptionalInt.of(DEFAULT_NUMBER_OF_COWBOYS);
            }
            final OptionalInt validCowboyCount = parseValidCowboyCount(enteredValue, output);
            if (validCowboyCount.isPresent()) {
                return validCowboyCount;
            }
        }
    }

    private static OptionalInt parseValidCowboyCount(String enteredValue, PrintStream output) {
        try {
            final int numberOfCowboys = Integer.parseInt(enteredValue.trim());
            ShootoutLimits.validateBatchCowboyCount(numberOfCowboys);
            return OptionalInt.of(numberOfCowboys);
        } catch (NumberFormatException invalidNumberException) {
            output.printf(INVALID_COUNT_FORMAT, WHOLE_NUMBER_REQUIRED);
        } catch (IllegalArgumentException invalidCountException) {
            output.printf(INVALID_COUNT_FORMAT, invalidCountException.getMessage());
        }
        return OptionalInt.empty();
    }
}