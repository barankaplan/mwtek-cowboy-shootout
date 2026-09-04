package com.mwtek.shootout.game;

/** Operational limits that prevent unreasonable memory, CPU, and disk usage. */
public final class ShootoutLimits {
    public static final int MAX_DETAILED_COWBOYS = 10_000;
    public static final int MAX_BATCH_COWBOYS = 1_000_000;
    public static final int MAX_BATCH_SIMULATIONS = 1_000_000;
    public static final long MAX_BATCH_COWBOY_GAMES = 10_000_000L;

    private ShootoutLimits() {
    }

    public static void validateDetailedCowboyCount(int numberOfCowboys) {
        validatePositiveCowboyCount(numberOfCowboys);
        if (numberOfCowboys > MAX_DETAILED_COWBOYS) {
            throw new IllegalArgumentException("Detailed protocol mode supports at most " + MAX_DETAILED_COWBOYS
                    + " cowboys; use batch mode for larger simulations");
        }
    }

    public static void validateBatchCowboyCount(int numberOfCowboys) {
        validatePositiveCowboyCount(numberOfCowboys);
        if (numberOfCowboys > MAX_BATCH_COWBOYS) {
            throw new IllegalArgumentException("Batch mode supports at most " + MAX_BATCH_COWBOYS + " cowboys");
        }
    }

    public static void validateBatchSimulationCount(int simulationCount) {
        if (simulationCount < 1) {
            throw new IllegalArgumentException("Batch size must be positive");
        }
        if (simulationCount > MAX_BATCH_SIMULATIONS) {
            throw new IllegalArgumentException("Batch mode supports at most " + MAX_BATCH_SIMULATIONS + " simulations");
        }
    }

    public static void validateBatchWorkload(int numberOfCowboys, int simulationCount) {
        final long cowboyGames = (long) numberOfCowboys * simulationCount;
        if (cowboyGames > MAX_BATCH_COWBOY_GAMES) {
            throw new IllegalArgumentException("Batch workload is too large: cowboy count multiplied by batch size must not exceed "
                    + MAX_BATCH_COWBOY_GAMES + ". Reduce the cowboy count or batch size");
        }
    }

    public static void validatePositiveCowboyCount(int numberOfCowboys) {
        if (numberOfCowboys < 1) {
            throw new IllegalArgumentException("Cowboy count must be positive");
        }
    }
}