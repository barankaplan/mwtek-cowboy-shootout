package com.mwtek.shootout.game;

import com.mwtek.shootout.game.random.RandomSource;
import com.mwtek.shootout.game.result.ShootoutResult;
import com.mwtek.shootout.game.result.ShootoutSummary;
import com.mwtek.shootout.game.result.ShotEvent;
import com.mwtek.shootout.game.result.Winner;
import com.mwtek.shootout.game.statistics.StatisticsCollector;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Plays a complete shootout according to the configured game rules. */
public final class ShootoutGame {
    private final RandomSource randomSource;
    private final DamageGenerator damageGenerator;

    public ShootoutGame(RandomSource randomSource) {
        this.randomSource = Objects.requireNonNull(randomSource, "randomSource");
        damageGenerator = new DamageGenerator(randomSource);
    }

    public ShootoutResult play(int numberOfCowboys) {
        ShootoutLimits.validateDetailedCowboyCount(numberOfCowboys);
        final int startingCowboyId = randomSource.nextInt(numberOfCowboys);
        return play(new ShootoutSetup(numberOfCowboys, startingCowboyId));
    }

    /** Plays a game with a selected starter; used for positional-fairness batches. */
    public ShootoutResult play(ShootoutSetup shootoutSetup) {
        Objects.requireNonNull(shootoutSetup, "shootoutSetup");
        ShootoutLimits.validateDetailedCowboyCount(shootoutSetup.numberOfCowboys());
        final int numberOfCowboys = shootoutSetup.numberOfCowboys();
        final ShootoutState shootoutState = new ShootoutState(shootoutSetup);
        final List<ShotEvent> shotHistory = new ArrayList<>();
        final StatisticsCollector statisticsCollector =
                new StatisticsCollector(numberOfCowboys);
        runShootout(shootoutState, statisticsCollector, shotHistory);
        return new ShootoutResult(
                numberOfCowboys,
                shootoutSetup.startingCowboyId(),
                shotHistory,
                shootoutState.getWinner(),
                statisticsCollector.snapshot());
    }

    /** Plays a game without retaining detailed shot events; intended for batch analysis. */
    public ShootoutSummary summarize(int numberOfCowboys) {
        ShootoutLimits.validateBatchCowboyCount(numberOfCowboys);
        final int startingCowboyId = randomSource.nextInt(numberOfCowboys);
        return summarize(new ShootoutSetup(numberOfCowboys, startingCowboyId));
    }

    /** Plays a fixed-starter game without retaining detailed shot events. */
    public ShootoutSummary summarize(ShootoutSetup shootoutSetup) {
        Objects.requireNonNull(shootoutSetup, "shootoutSetup");
        ShootoutLimits.validateBatchCowboyCount(shootoutSetup.numberOfCowboys());
        final ShootoutState shootoutState = new ShootoutState(shootoutSetup);
        final StatisticsCollector statisticsCollector =
                new StatisticsCollector(shootoutSetup.numberOfCowboys());
        final int totalShots = runShootout(shootoutState, statisticsCollector, null);
        final Winner winner = shootoutState.getWinner();
        return new ShootoutSummary(
                shootoutSetup.numberOfCowboys(),
                shootoutSetup.startingCowboyId(),
                totalShots,
                winner,
                statisticsCollector.snapshotForCowboy(winner.cowboyId()));
    }

    /** A null history deliberately prevents ShotEvent allocation in batch mode. */
    private int runShootout(
            ShootoutState shootoutState, StatisticsCollector statisticsCollector, List<ShotEvent> shotHistoryOrNull) {
        int shotNumber = 0;
        while (!shootoutState.isComplete()) {
            playNextShot(
                    shootoutState, statisticsCollector, shotHistoryOrNull, ++shotNumber);
        }
        return shotNumber;
    }

    private void playNextShot(
            ShootoutState shootoutState, StatisticsCollector statisticsCollector,
            List<ShotEvent> shotHistoryOrNull, int shotNumber) {
        final int shooterCowboyId = shootoutState.getCurrentCowboyId();
        final int shooterHealthPoints = shootoutState.getHealthPoints(shooterCowboyId);
        final int activeCowboyTurnNumber = shootoutState.getActiveCowboyTurnNumber();
        final Direction direction = determineDirection(shooterHealthPoints);
        final int targetCowboyId = shootoutState.getNeighbor(shooterCowboyId, direction);
        final int damageRolled = damageGenerator.nextDamage();
        final DamageOutcome damageOutcome =
                shootoutState.applyDamageToCowboy(targetCowboyId, damageRolled);

        if (damageOutcome.killed()) {
            shootoutState.eliminateCowboy(targetCowboyId);
        } else {
            shootoutState.passTurnToCowboy(targetCowboyId);
        }
        statisticsCollector.recordShot(
                shooterCowboyId,
                targetCowboyId,
                damageOutcome.effectiveHealthLost(),
                damageOutcome.killed());
        if (shotHistoryOrNull == null) {
            return;
        }
        shotHistoryOrNull.add(new ShotEvent(
                shotNumber,
                activeCowboyTurnNumber,
                shooterCowboyId,
                shooterHealthPoints,
                direction,
                targetCowboyId,
                damageOutcome.healthBefore(),
                damageOutcome.rolledDamage(),
                damageOutcome.effectiveHealthLost(),
                damageOutcome.healthAfter(),
                damageOutcome.overkill(),
                damageOutcome.killed(),
                shootoutState.getRemainingCowboyCount()));
    }

    private static Direction determineDirection(int shooterHealthPoints) {
        return shooterHealthPoints % 2 == 0 ? Direction.RIGHT : Direction.LEFT;
    }
}