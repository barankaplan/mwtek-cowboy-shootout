package com.mwtek.shootout.game.result;

import com.mwtek.shootout.game.statistics.WinnerStatistics;
import java.util.Objects;

/** Compact result used when a shot-by-shot audit trail is not required. */
public record ShootoutSummary(
        int numberOfCowboys,
        int startingCowboyId,
        int totalShots,
        Winner winner,
        WinnerStatistics winnerStatistics) {
    public ShootoutSummary {
        Objects.requireNonNull(winner, "winner");
        Objects.requireNonNull(winnerStatistics, "winnerStatistics");
    }

    public WinnerPosition winnerPosition() {
        return WinnerPosition.calculate(
                numberOfCowboys, startingCowboyId, winner.cowboyId());
    }
}