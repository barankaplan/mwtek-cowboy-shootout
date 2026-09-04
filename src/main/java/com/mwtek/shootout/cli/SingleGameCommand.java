package com.mwtek.shootout.cli;

import java.nio.file.Path;
import java.util.Objects;
import java.util.OptionalLong;
import com.mwtek.shootout.game.ShootoutLimits;

/** Parameters for one shootout and its detailed JSON protocol. */
public record SingleGameCommand(int numberOfCowboys, OptionalLong randomSeed, Path protocolOutputPath) implements CliCommand {

    public SingleGameCommand {
        ShootoutLimits.validateDetailedCowboyCount(numberOfCowboys);
        Objects.requireNonNull(randomSeed, "Random seed container must not be null");
        Objects.requireNonNull(protocolOutputPath, "Protocol output path must not be null");
    }
}