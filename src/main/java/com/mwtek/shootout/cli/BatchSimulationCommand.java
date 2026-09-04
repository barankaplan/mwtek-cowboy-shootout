package com.mwtek.shootout.cli;

import com.mwtek.shootout.batch.BatchSimulationPlan;
import java.nio.file.Path;
import java.util.Objects;

/** Parameters for a batch experiment and its compact CSV summary. */
public record BatchSimulationCommand(
        BatchSimulationPlan simulationPlan,
        Path summaryOutputPath) implements CliCommand {

    public BatchSimulationCommand {
        Objects.requireNonNull(simulationPlan, "Simulation plan must not be null");
        Objects.requireNonNull(summaryOutputPath, "Summary output path must not be null");
    }
}