package com.mwtek.shootout.batch;

import com.mwtek.shootout.game.result.ShootoutSummary;
import com.mwtek.shootout.game.result.Winner;
import com.mwtek.shootout.game.result.WinnerPosition;
import com.mwtek.shootout.game.statistics.WinnerStatistics;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Values written for one completed game in the batch summary CSV. */
record SimulationSummaryRow(
        int simulationId,
        int cowboyCount,
        long gameSeed,
        int starterCowboyId,
        int winnerCowboyId,
        int winnerOffsetFromStarter,
        int totalShots,
        int winnerRemainingHealthPoints,
        int winnerShotsFired,
        int winnerKills,
        int winnerTotalDamageDealt) {

    static final String CSV_HEADER = String.join(",",
            "simulationId",
            "cowboyCount",
            "seed",
            "starterId",
            "winnerId",
            "winnerOffsetFromStarter",
            "totalShots",
            "winnerRemainingHp",
            "winnerShotsFired",
            "winnerKills",
            "winnerTotalDamageDealt");

    static SimulationSummaryRow from(int simulationId, long gameSeed, ShootoutSummary shootoutSummary) {
        final Winner winner = shootoutSummary.winner();
        final WinnerPosition winnerPosition = shootoutSummary.winnerPosition();
        final WinnerStatistics winnerStatistics = shootoutSummary.winnerStatistics();

        return new SimulationSummaryRow(
                simulationId,
                shootoutSummary.numberOfCowboys(),
                gameSeed,
                shootoutSummary.startingCowboyId(),
                winner.cowboyId(),
                winnerPosition.offsetFromStarter(),
                shootoutSummary.totalShots(),
                winner.remainingHealthPoints(),
                winnerStatistics.shotsFired(),
                winnerStatistics.kills(),
                winnerStatistics.totalDamageDealt());
    }

    String toCsvRow() {
        return Stream.of(
                        simulationId,
                        cowboyCount,
                        gameSeed,
                        starterCowboyId,
                        winnerCowboyId,
                        winnerOffsetFromStarter,
                        totalShots,
                        winnerRemainingHealthPoints,
                        winnerShotsFired,
                        winnerKills,
                        winnerTotalDamageDealt)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

}