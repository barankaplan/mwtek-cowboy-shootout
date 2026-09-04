package com.mwtek.shootout.batch;

import com.mwtek.shootout.game.ShootoutGame;
import com.mwtek.shootout.game.ShootoutSetup;
import com.mwtek.shootout.game.random.JavaRandomSource;
import com.mwtek.shootout.game.result.ShootoutSummary;
import com.mwtek.shootout.output.AtomicFileWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Random;

/**
 * Writes one compact row per game, avoiding a large shot-level audit trail in batches.
 */
public final class BatchSimulationWriter {
    private static Random createGameSeedGenerator(BatchSimulationPlan simulationPlan) {
        return simulationPlan.masterSeed().isPresent()
                ? new Random(simulationPlan.masterSeed().getAsLong())
                : new Random();
    }

    private static void writeSimulationSummary(BatchSimulationPlan simulationPlan, Path summaryOutputPath, Random gameSeedGenerator) throws IOException {
        try (BufferedWriter summaryWriter = Files.newBufferedWriter(summaryOutputPath, StandardCharsets.UTF_8)) {
            writeHeader(summaryWriter);
            writeSimulationRows(summaryWriter, simulationPlan, gameSeedGenerator);
        }
    }

    private static void writeHeader(BufferedWriter summaryWriter) throws IOException {
        summaryWriter.write(SimulationSummaryRow.CSV_HEADER);
        summaryWriter.newLine();
    }

    private static void writeSimulationRows(BufferedWriter summaryWriter, BatchSimulationPlan simulationPlan, Random gameSeedGenerator) throws IOException {
        for (int simulationIndex = 0;
                simulationIndex < simulationPlan.simulationCount();
                simulationIndex++) {
            final int simulationId = simulationIndex + 1;
            final long gameSeed = gameSeedGenerator.nextLong();
            final SimulationSummaryRow summaryRow = runSimulation(simulationId, gameSeed, simulationPlan);
            summaryWriter.write(summaryRow.toCsvRow());
            summaryWriter.newLine();
        }
    }

    private static SimulationSummaryRow runSimulation(int simulationId, long gameSeed, BatchSimulationPlan simulationPlan) {
        final ShootoutGame shootoutGame = new ShootoutGame(new JavaRandomSource(gameSeed));
        final ShootoutSummary shootoutSummary = summarizeShootout(shootoutGame, simulationPlan);
        return SimulationSummaryRow.from(simulationId, gameSeed, shootoutSummary);
    }

    private static ShootoutSummary summarizeShootout(ShootoutGame shootoutGame, BatchSimulationPlan simulationPlan) {
        return simulationPlan.fixedStarterCowboyId().isPresent()
                ? shootoutGame.summarize(new ShootoutSetup(simulationPlan.numberOfCowboys(), simulationPlan.fixedStarterCowboyId().getAsInt()))
                : shootoutGame.summarize(simulationPlan.numberOfCowboys());
    }

    public void write(BatchSimulationPlan simulationPlan, Path summaryOutputPath) throws IOException {
        Objects.requireNonNull(simulationPlan, "simulationPlan");
        Objects.requireNonNull(summaryOutputPath, "summaryOutputPath");
        final Random gameSeedGenerator = createGameSeedGenerator(simulationPlan);
        AtomicFileWriter.write(summaryOutputPath, temporaryFile -> writeSimulationSummary(simulationPlan, temporaryFile, gameSeedGenerator));
    }
}