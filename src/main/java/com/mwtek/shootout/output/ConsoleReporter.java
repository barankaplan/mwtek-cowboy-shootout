package com.mwtek.shootout.output;

import com.mwtek.shootout.game.result.ShootoutResult;
import com.mwtek.shootout.game.result.ShotEvent;
import com.mwtek.shootout.game.result.Winner;
import com.mwtek.shootout.game.result.WinnerPosition;
import java.util.Locale;
import java.util.Objects;

/** Displays a detailed shootout in the terminal. */
public final class ConsoleReporter {
    private static final String SHOOTOUT_STARTED_FORMAT = "Shootout started with %d %s%nStarting cowboy: %d%n";
    private static final String SHOT_FORMAT = "#%d Cowboy %d shoots %s at Cowboy %d; damage=%d, target HP=%d%s%n";
    private static final String WINNER_FORMAT = "Winner: Cowboy %d with %d HP%n";
    private static final String WINNER_POSITION_FORMAT = """
            Starter-to-winner position: Cowboy %d -> Cowboy %d, offset +%d/%d
            Distance from starter: %.1f%% toward LEFT, equivalently %.1f%% toward RIGHT
            """;
    private static final String SOLE_COWBOY_EXPLANATION = "No shots were needed because Cowboy %d was the only participant.%n";
    private static final String KILLED_SUFFIX = " (killed)";

    public void reportShootoutStarted(int numberOfCowboys, int startingCowboyId) {
        final String cowboyWord = numberOfCowboys == 1 ? "cowboy" : "cowboys";
        System.out.printf(SHOOTOUT_STARTED_FORMAT, numberOfCowboys, cowboyWord, startingCowboyId);
    }

    public void reportShot(ShotEvent shot) {
        Objects.requireNonNull(shot, "shot");
        System.out.printf(SHOT_FORMAT,
                shot.shotNumber(), shot.shooterCowboyId(), shot.direction(), shot.targetCowboyId(),
                shot.damageRolled(), shot.targetHealthPointsAfter(),
                shot.killed() ? KILLED_SUFFIX : "");
    }

    public void reportShootoutCompleted(ShootoutResult shootoutResult) {
        Objects.requireNonNull(shootoutResult, "shootoutResult");
        final Winner winner = shootoutResult.winner();
        System.out.printf(WINNER_FORMAT, winner.cowboyId(), winner.remainingHealthPoints());
        if (shootoutResult.numberOfCowboys() == 1) {
            System.out.printf(SOLE_COWBOY_EXPLANATION, winner.cowboyId());
            return;
        }
        final WinnerPosition winnerPosition = shootoutResult.winnerPosition();
        System.out.printf(Locale.ROOT, WINNER_POSITION_FORMAT,
                shootoutResult.startingCowboyId(), winner.cowboyId(),
                winnerPosition.offsetFromStarter(), shootoutResult.numberOfCowboys(),
                winnerPosition.percentageTowardLeft(), winnerPosition.percentageTowardRight());
    }
}