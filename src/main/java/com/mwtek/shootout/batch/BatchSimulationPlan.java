package com.mwtek.shootout.batch;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.OptionalLong;
import com.mwtek.shootout.game.ShootoutLimits;

/** Immutable, validated parameters for a repeatable simulation experiment. */
public record BatchSimulationPlan(int numberOfCowboys, int simulationCount, OptionalLong masterSeed, OptionalInt fixedStarterCowboyId) {

    public BatchSimulationPlan {
        ShootoutLimits.validateBatchCowboyCount(numberOfCowboys);
        ShootoutLimits.validateBatchSimulationCount(simulationCount);
        ShootoutLimits.validateBatchWorkload(numberOfCowboys, simulationCount);
        Objects.requireNonNull(masterSeed, "Master seed container must not be null");
        Objects.requireNonNull(fixedStarterCowboyId, "Starter container must not be null");
        fixedStarterCowboyId.ifPresent(starterCowboyId -> validateStarterCowboyId(starterCowboyId, numberOfCowboys));
    }

    private static void validateStarterCowboyId(int starterCowboyId, int numberOfCowboys) {
        if (starterCowboyId < 0 || starterCowboyId >= numberOfCowboys) {
            throw new IllegalArgumentException("Starter must be a valid cowboy ID");
        }
    }
}