package com.mwtek.shootout.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mwtek.shootout.cli.BatchSimulationCommand;
import com.mwtek.shootout.cli.CliCommand;
import com.mwtek.shootout.cli.SingleGameCommand;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {
    @TempDir Path temporaryDirectory;

    @Test
    void generatedSeedIsReportedAndCanReplayTheGame() throws Exception {
        Path firstProtocol = temporaryDirectory.resolve("first.json");
        Path replayProtocol = temporaryDirectory.resolve("replay.json");
        ByteArrayOutputStream consoleBytes = new ByteArrayOutputStream();
        PrintStream originalStandardOutput = System.out;

        try (PrintStream capturedOutput = new PrintStream(consoleBytes, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            Main.main(new String[]{"3", "--output", firstProtocol.toString()});

            JsonNode firstGame = new ObjectMapper().readTree(firstProtocol.toFile());
            assertTrue(firstGame.path("seed").isIntegralNumber());
            long generatedSeed = firstGame.path("seed").asLong();
            assertTrue(consoleBytes.toString(StandardCharsets.UTF_8)
                    .contains("Seed: " + generatedSeed));

            Main.main(new String[]{
                "3", "--seed", Long.toString(generatedSeed),
                "--output", replayProtocol.toString()
            });
            JsonNode replayedGame = new ObjectMapper().readTree(replayProtocol.toFile());
            assertEquals(firstGame, replayedGame);
        } finally {
            System.setOut(originalStandardOutput);
        }
    }

    @Test
    void interactiveModeAutomaticallyChoosesACompactBatchForLargeCircles() {
        final CliCommand detailedCommand = Main.createInteractiveCommand(10_000);
        final CliCommand compactCommand = Main.createInteractiveCommand(500_111);

        assertTrue(detailedCommand instanceof SingleGameCommand);
        final BatchSimulationCommand batchCommand = (BatchSimulationCommand) compactCommand;
        assertEquals(500_111, batchCommand.simulationPlan().numberOfCowboys());
        assertEquals(10, batchCommand.simulationPlan().simulationCount());
        assertTrue(batchCommand.summaryOutputPath().toString().endsWith(".csv"));
    }
}