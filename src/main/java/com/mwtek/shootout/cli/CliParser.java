package com.mwtek.shootout.cli;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.OptionalLong;

import com.mwtek.shootout.batch.BatchSimulationPlan;

/** Parses command-line arguments into typed application configuration. */
public final class CliParser {
    private static final Path DEFAULT_PROTOCOL_PATH = Path.of("shootout-protocols", "cowboy-shootout-protocol.json");
    private static final Path DEFAULT_SUMMARY_PATH = Path.of("analysis/output/simulation-summary.csv");

    private CliParser() {
    }

    public static CliCommand parse(String[] commandLineArguments) {
        Objects.requireNonNull(commandLineArguments, "Command-line arguments must not be null");
        if (commandLineArguments.length == 0) {
            throw new IllegalArgumentException("Cowboy count is required");
        }
        final int numberOfCowboys = parsePositiveInt(commandLineArguments[0], "Cowboy count");
        final Map<CliOption, String> optionValues = collectOptionValues(commandLineArguments);
        if (optionValues.containsKey(CliOption.BATCH)) {
            return createBatchSimulationCommand(numberOfCowboys, optionValues);
        }
        return createSingleGameCommand(numberOfCowboys, optionValues);
    }

    private static Map<CliOption, String> collectOptionValues(String[] commandLineArguments) {
        final Map<CliOption, String> optionValues = new EnumMap<>(CliOption.class);
        for (int argumentIndex = 1; argumentIndex < commandLineArguments.length; argumentIndex++) {
            final CliOption option = CliOption.fromToken(commandLineArguments[argumentIndex]);
            final int valueIndex = ++argumentIndex;
            if (valueIndex >= commandLineArguments.length) {
                throw new IllegalArgumentException("Option requires a value: " + option.token());
            }
            final String previousValue = optionValues.putIfAbsent(option, commandLineArguments[valueIndex]);
            if (previousValue != null) {
                throw new IllegalArgumentException("Option may only be specified once: " + option.token());
            }
        }
        return optionValues;
    }

    private static SingleGameCommand createSingleGameCommand(int numberOfCowboys, Map<CliOption, String> optionValues) {
        rejectOptionOutsideBatchMode(optionValues, CliOption.STARTER);
        rejectOptionOutsideBatchMode(optionValues, CliOption.SUMMARY);
        final OptionalLong randomSeed = parseOptionalSeed(optionValues.get(CliOption.SEED));
        final Path protocolOutputPath = pathOrDefault(optionValues, CliOption.OUTPUT, DEFAULT_PROTOCOL_PATH);
        return new SingleGameCommand(numberOfCowboys, randomSeed, protocolOutputPath);
    }

    private static BatchSimulationCommand createBatchSimulationCommand(int numberOfCowboys, Map<CliOption, String> optionValues) {
        if (optionValues.containsKey(CliOption.OUTPUT)) {
            throw new IllegalArgumentException("--output cannot be used with --batch; use --summary");
        }
        final int simulationCount = parsePositiveInt(optionValues.get(CliOption.BATCH), "Batch size");
        final OptionalInt fixedStarter = parseOptionalStarter(optionValues.get(CliOption.STARTER));
        final OptionalLong randomSeed = parseOptionalSeed(optionValues.get(CliOption.SEED));
        final Path summaryOutputPath = pathOrDefault(optionValues, CliOption.SUMMARY, DEFAULT_SUMMARY_PATH);
        final BatchSimulationPlan plan = new BatchSimulationPlan(numberOfCowboys, simulationCount, randomSeed, fixedStarter);
        return new BatchSimulationCommand(plan, summaryOutputPath);
    }

    private static void rejectOptionOutsideBatchMode(Map<CliOption, String> optionValues, CliOption batchOnlyOption) {
        if (optionValues.containsKey(batchOnlyOption)) {
            throw new IllegalArgumentException(batchOnlyOption.token() + " is only valid with --batch");
        }
    }

    private static Path pathOrDefault(Map<CliOption, String> optionValues, CliOption option, Path defaultPath) {
        return optionValues.containsKey(option) ? Path.of(optionValues.get(option)) : defaultPath;
    }

    private static int parsePositiveInt(String value, String label) {
        final int parsedValue = parseInt(value, label);
        if (parsedValue < 1) {
            throw new IllegalArgumentException(label + " must be positive");
        }
        return parsedValue;
    }

    private static OptionalInt parseOptionalStarter(String value) {
        return value == null ? OptionalInt.empty() : OptionalInt.of(parseInt(value, "Starter"));
    }

    private static OptionalLong parseOptionalSeed(String value) {
        return value == null ? OptionalLong.empty() : OptionalLong.of(parseSeed(value));
    }

    private static int parseInt(String value, String label) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException invalidNumberException) {
            throw new IllegalArgumentException(label + " must be an integer", invalidNumberException);
        }
    }

    private static long parseSeed(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException invalidNumberException) {
            throw new IllegalArgumentException("Seed must be a long integer", invalidNumberException);
        }
    }
}