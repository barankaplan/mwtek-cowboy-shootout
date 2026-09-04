package com.mwtek.shootout.game;

/** Validated starting configuration for one shootout. */
public record ShootoutSetup(int numberOfCowboys, int startingCowboyId) {
    public ShootoutSetup {
        ShootoutLimits.validateBatchCowboyCount(numberOfCowboys);
        validateStartingCowboyId(startingCowboyId, numberOfCowboys);
    }

    private static void validateStartingCowboyId(int startingCowboyId, int numberOfCowboys) {
        if (startingCowboyId < 0 || startingCowboyId >= numberOfCowboys) {
            throw new IllegalArgumentException("Starting cowboy is outside the shootout");
        }
    }
}