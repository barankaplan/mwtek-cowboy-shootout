package com.mwtek.shootout.cli;

import java.io.PrintStream;
import java.util.Objects;

/** Prints the supported command-line forms in one consistent place. */
public final class CliUsage {
    private static final String USAGE_TEXT = """
            Usage: java -jar target/cowboy-shootout-1.0.0.jar [<positive-count>] \
            [--seed N] [--output file.json]
               or: java -jar target/cowboy-shootout-1.0.0.jar <positive-count> \
            --batch N [--starter ID] [--seed N] [--summary file.csv]
            Without arguments, the program asks for the cowboy count after each game;
            Enter uses 5 and Q quits.
            """;

    private CliUsage() {
    }

    public static void printTo(PrintStream output) {
        Objects.requireNonNull(output, "output").print(USAGE_TEXT);
    }
}