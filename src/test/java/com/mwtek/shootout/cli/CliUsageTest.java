package com.mwtek.shootout.cli;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;

class CliUsageTest {
    @Test
    void printsBothSupportedCommandForms() {
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();

        CliUsage.printTo(new PrintStream(outputBytes, true, UTF_8));

        assertEquals("""
                Usage: java -jar target/cowboy-shootout-1.0.0.jar [<positive-count>] [--seed N] [--output file.json]
                   or: java -jar target/cowboy-shootout-1.0.0.jar <positive-count> \
                --batch N [--starter ID] [--seed N] [--summary file.csv]
                Without arguments, the program asks for the cowboy count after each game;
                Enter uses 5 and Q quits.
                """, outputBytes.toString(UTF_8));
    }
}