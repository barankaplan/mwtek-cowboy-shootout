package com.mwtek.shootout.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.mwtek.shootout.game.ShootoutLimits;
import org.junit.jupiter.api.Test;

class CliParserTest {
    @Test
    void parsesTypedSingleGameArguments() {
        CliCommand parsedCommand = CliParser.parse(
                new String[]{"5", "--seed", "123", "--output", "game.json"});

        SingleGameCommand command = assertInstanceOf(SingleGameCommand.class, parsedCommand);
        assertEquals(5, command.numberOfCowboys());
        assertEquals(123L, command.randomSeed().orElseThrow());
        assertEquals("game.json", command.protocolOutputPath().toString());
    }

    @Test
    void usesARecognizableProtocolArchivePathByDefault() {
        final SingleGameCommand command = assertInstanceOf(
                SingleGameCommand.class, CliParser.parse(new String[]{"5"}));

        assertEquals(
                "shootout-protocols/cowboy-shootout-protocol.json",
                command.protocolOutputPath().toString());
    }

    @Test
    void parsesBatchArgumentsWithAForcedStarter() {
        CliCommand parsedCommand = CliParser.parse(
                new String[]{"5", "--batch", "100", "--starter", "0", "--summary", "summary.csv"});

        BatchSimulationCommand command = assertInstanceOf(BatchSimulationCommand.class, parsedCommand);
        assertEquals(100, command.simulationPlan().simulationCount());
        assertEquals(0, command.simulationPlan().fixedStarterCowboyId().orElseThrow());
        assertEquals("summary.csv", command.summaryOutputPath().toString());
    }

    @Test
    void acceptsOneMillionCowboysWhenTheTotalBatchWorkloadIsWithinTheLimit() {
        final BatchSimulationCommand command = assertInstanceOf(
                BatchSimulationCommand.class,
                CliParser.parse(new String[]{"1000000", "--batch", "10"}));

        assertEquals(1_000_000, command.simulationPlan().numberOfCowboys());
        assertEquals(10, command.simulationPlan().simulationCount());
    }

    @Test
    void rejectsInvalidCountsAndIncompatibleOptions() {
        assertThrows(IllegalArgumentException.class, () -> CliParser.parse(new String[]{"0"}));
        assertThrows(IllegalArgumentException.class, () -> CliParser.parse(new String[]{"five"}));
        assertThrows(IllegalArgumentException.class, () -> CliParser.parse(new String[]{"5", "--starter", "0"}));
        assertThrows(IllegalArgumentException.class,
                () -> CliParser.parse(new String[]{"5", "--batch", "5", "--output", "game.json"}));
        assertThrows(IllegalArgumentException.class,
                () -> CliParser.parse(new String[]{"5", "--batch", "5", "--starter", "5"}));
        assertThrows(IllegalArgumentException.class,
                () -> CliParser.parse(new String[]{"5", "--seed", "1", "--seed", "2"}));
        assertThrows(IllegalArgumentException.class, () -> CliParser.parse(
                new String[]{String.valueOf(ShootoutLimits.MAX_DETAILED_COWBOYS + 1)}));
        assertThrows(IllegalArgumentException.class, () -> CliParser.parse(
                new String[]{"5", "--batch",
                    String.valueOf(ShootoutLimits.MAX_BATCH_SIMULATIONS + 1)}));
        assertThrows(IllegalArgumentException.class, () -> CliParser.parse(
                new String[]{String.valueOf(ShootoutLimits.MAX_BATCH_COWBOYS),
                    "--batch", "101"}));
    }
}