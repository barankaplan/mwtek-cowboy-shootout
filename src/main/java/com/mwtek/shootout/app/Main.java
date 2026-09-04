package com.mwtek.shootout.app;

import com.mwtek.shootout.batch.BatchSimulationWriter;
import com.mwtek.shootout.batch.BatchSimulationPlan;
import com.mwtek.shootout.cli.BatchSimulationCommand;
import com.mwtek.shootout.cli.CliCommand;
import com.mwtek.shootout.cli.CliParser;
import com.mwtek.shootout.cli.CliUsage;
import com.mwtek.shootout.cli.CowboyCountPrompt;
import com.mwtek.shootout.cli.SingleGameCommand;
import com.mwtek.shootout.game.ShootoutGame;
import com.mwtek.shootout.game.ShootoutLimits;
import com.mwtek.shootout.game.random.JavaRandomSource;
import com.mwtek.shootout.game.result.ShootoutResult;
import com.mwtek.shootout.output.ChecksumCalculator;
import com.mwtek.shootout.output.ConsoleReporter;
import com.mwtek.shootout.output.ProtocolWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("java:S106") // The assignment explicitly requires status and error messages in the console.
public final class Main {
    private static final String ARGUMENT_ERROR_FORMAT = "Error: %s%n";
    private static final String CORRECT_ARGUMENTS_MESSAGE = "The command was not run. Correct the arguments and run it again.%n";
    private static final String IO_ERROR_FORMAT = "I/O error: %s%n";
    private static final String PROTOCOL_ARCHIVED_FORMAT = "Protocol archived as JSON: %s%n";
    private static final String CHECKSUM_FORMAT = "SHA-256 checksum: %s%n";
    private static final String SEED_FORMAT = "Seed: %d%n";
    private static final String BATCH_COMPLETED_FORMAT = "Batch summary with %d simulations written to %s%n";
    private static final String AUTOMATIC_BATCH_FORMAT = "Large circle selected: running %d compact simulations without shot-by-shot JSON.%n";
    private static final int INTERACTIVE_BATCH_SIMULATION_COUNT = 10;
    private static final Path INTERACTIVE_PROTOCOL_ARCHIVE_DIRECTORY = Path.of("shootout-protocols");
    private static final String INTERACTIVE_PROTOCOL_FILE_FORMAT = "cowboy-shootout-protocol-seed-%d.json";
    private static final Path INTERACTIVE_BATCH_OUTPUT_DIRECTORY = Path.of("analysis", "output");
    private static final String INTERACTIVE_BATCH_FILE_FORMAT = "cowboy-shootout-batch-%d-cowboys-seed-%d.csv";
    private static final String GOODBYE_MESSAGE = "Shootout closed.%n";

    private Main() { }

    public static void main(String[] commandLineArguments) {
        try {
            if (commandLineArguments.length == 0) {
                runInteractiveGames();
            } else {
                runCommand(CliParser.parse(commandLineArguments));
            }
        } catch (IllegalArgumentException invalidArgumentsException) {
            System.err.printf(ARGUMENT_ERROR_FORMAT, invalidArgumentsException.getMessage());
            System.err.printf(CORRECT_ARGUMENTS_MESSAGE);
            CliUsage.printTo(System.err);
            System.exit(2);
        } catch (IOException outputException) {
            System.err.printf(IO_ERROR_FORMAT, outputException.getMessage());
            System.exit(1);
        }
    }

    private static void runInteractiveGames() throws IOException {
        final BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        while (true) {
            final OptionalInt requestedCowboyCount = CowboyCountPrompt.read(consoleInput, System.out);
            if (requestedCowboyCount.isEmpty()) {
                System.out.printf(GOODBYE_MESSAGE);
                return;
            }
            final CliCommand interactiveCommand = createInteractiveCommand(requestedCowboyCount.getAsInt());
            if (interactiveCommand instanceof BatchSimulationCommand batchCommand) {
                final int simulationCount = batchCommand.simulationPlan().simulationCount();
                System.out.printf(AUTOMATIC_BATCH_FORMAT, simulationCount);
            }
            runCommand(interactiveCommand);
        }
    }

    static CliCommand createInteractiveCommand(int numberOfCowboys) {
        ShootoutLimits.validateBatchCowboyCount(numberOfCowboys);
        return numberOfCowboys <= ShootoutLimits.MAX_DETAILED_COWBOYS
                ? createInteractiveGameCommand(numberOfCowboys)
                : createInteractiveBatchCommand(numberOfCowboys);
    }

    private static SingleGameCommand createInteractiveGameCommand(int numberOfCowboys) {
        while (true) {
            final long randomSeed = ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
            final String protocolFileName = INTERACTIVE_PROTOCOL_FILE_FORMAT.formatted(randomSeed);
            final Path protocolOutputPath = INTERACTIVE_PROTOCOL_ARCHIVE_DIRECTORY.resolve(protocolFileName);
            if (!Files.exists(protocolOutputPath)) {
                return new SingleGameCommand(numberOfCowboys, OptionalLong.of(randomSeed), protocolOutputPath);
            }
        }
    }

    private static BatchSimulationCommand createInteractiveBatchCommand(int numberOfCowboys) {
        while (true) {
            final long masterSeed = ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
            final String summaryFileName = INTERACTIVE_BATCH_FILE_FORMAT.formatted(numberOfCowboys, masterSeed);
            final Path summaryOutputPath = INTERACTIVE_BATCH_OUTPUT_DIRECTORY.resolve(summaryFileName);
            if (!Files.exists(summaryOutputPath)) {
                final OptionalLong masterSeedOption = OptionalLong.of(masterSeed);
                final OptionalInt randomStarter = OptionalInt.empty();
                final BatchSimulationPlan simulationPlan = new BatchSimulationPlan(numberOfCowboys, INTERACTIVE_BATCH_SIMULATION_COUNT,
                        masterSeedOption, randomStarter);
                return new BatchSimulationCommand(simulationPlan, summaryOutputPath);
            }
        }
    }

    private static void runCommand(CliCommand command) throws IOException {
        switch (command) {
            case SingleGameCommand singleGame -> runSingleGame(singleGame);
            case BatchSimulationCommand batchSimulation -> runBatchSimulation(batchSimulation);
        }
    }

    private static void runSingleGame(SingleGameCommand command) throws IOException {
        final long randomSeed = command.randomSeed().orElseGet(() -> ThreadLocalRandom.current().nextLong());
        final JavaRandomSource randomSource = new JavaRandomSource(randomSeed);
        final ShootoutGame shootoutGame = new ShootoutGame(randomSource);
        final ShootoutResult shootoutResult = shootoutGame.play(command.numberOfCowboys());
        final ConsoleReporter consoleReporter = new ConsoleReporter();
        consoleReporter.reportShootoutStarted(shootoutResult.numberOfCowboys(), shootoutResult.startingCowboyId());
        shootoutResult.shots().forEach(consoleReporter::reportShot);
        consoleReporter.reportShootoutCompleted(shootoutResult);
        new ProtocolWriter().write(shootoutResult, randomSeed, command.protocolOutputPath());
        System.out.printf(SEED_FORMAT, randomSeed);
        System.out.printf(PROTOCOL_ARCHIVED_FORMAT, command.protocolOutputPath().normalize());
        final String checksum = ChecksumCalculator.sha256(command.protocolOutputPath());
        System.out.printf(CHECKSUM_FORMAT, checksum);
    }

    private static void runBatchSimulation(BatchSimulationCommand command) throws IOException {
        new BatchSimulationWriter().write(command.simulationPlan(), command.summaryOutputPath());
        final int simulationCount = command.simulationPlan().simulationCount();
        final Path summaryOutputPath = command.summaryOutputPath().normalize();
        System.out.printf(BATCH_COMPLETED_FORMAT, simulationCount, summaryOutputPath);
    }
}