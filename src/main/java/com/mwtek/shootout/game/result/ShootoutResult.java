package com.mwtek.shootout.game.result;

import com.mwtek.shootout.game.statistics.GameStatistics;
import java.util.List;
import java.util.Objects;

/** Detailed completed game, including its immutable shot history. */
public record ShootoutResult(
        int numberOfCowboys,
        int startingCowboyId,
        List<ShotEvent> shots,
        Winner winner,
        GameStatistics statistics) {
    public ShootoutResult {
        shots = List.copyOf(Objects.requireNonNull(shots, "shots"));
        Objects.requireNonNull(winner, "winner");
        Objects.requireNonNull(statistics, "statistics");
    }

    public WinnerPosition winnerPosition() {
        return WinnerPosition.calculate(
                numberOfCowboys, startingCowboyId, winner.cowboyId());
    }
}