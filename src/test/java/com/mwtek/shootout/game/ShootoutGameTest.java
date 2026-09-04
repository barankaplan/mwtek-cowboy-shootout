package com.mwtek.shootout.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.mwtek.shootout.game.random.JavaRandomSource;
import com.mwtek.shootout.game.random.RandomSource;
import com.mwtek.shootout.game.result.ShootoutResult;
import com.mwtek.shootout.game.result.ShootoutSummary;
import com.mwtek.shootout.game.result.ShotEvent;
import com.mwtek.shootout.game.result.Winner;
import com.mwtek.shootout.game.result.WinnerPosition;
import com.mwtek.shootout.game.statistics.CowboyStatistics;
import com.mwtek.shootout.game.statistics.GameStatistics;
import com.mwtek.shootout.game.statistics.WinnerStatistics;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import org.junit.jupiter.api.Test;

class ShootoutGameTest {
    @Test
    void singleCowboyProducesNoShotAndWins() {
        ShootoutResult shootoutResult = new ShootoutGame(new PredeterminedRandomSource(0)).play(1);
        assertEquals(0, shootoutResult.shots().size());
        assertEquals(new Winner(0, 10), shootoutResult.winner());
    }

    @Test
    void rejectsInvalidCowboyCountBeforeRequestingRandomStarter() {
        RandomSource randomSourceThatMustNotBeCalled = ignored -> {
            throw new AssertionError("Random source must not run before validation");
        };
        final ShootoutGame shootoutGame = new ShootoutGame(randomSourceThatMustNotBeCalled);

        assertThrows(IllegalArgumentException.class, () -> shootoutGame.play(0));
        assertThrows(IllegalArgumentException.class, () -> shootoutGame.play(ShootoutLimits.MAX_DETAILED_COWBOYS + 1));
        assertThrows(IllegalArgumentException.class, () -> shootoutGame.summarize(ShootoutLimits.MAX_BATCH_COWBOYS + 1));
    }

    @Test
    void evenHpShootsRightAndSurvivingTargetBecomesCurrent() {
        // starter 0; damage 1 => cowboy 1 has odd HP and fires left next.
        ShootoutResult shootoutResult =
                new ShootoutGame(new PredeterminedRandomSource(0, 0, 0, 4, 4)).play(2);
        ShotEvent firstShot = shootoutResult.shots().getFirst();
        ShotEvent secondShot = shootoutResult.shots().get(1);
        assertEquals(Direction.RIGHT, firstShot.direction());
        assertEquals(1, firstShot.targetCowboyId());
        assertFalse(firstShot.killed());
        assertEquals(1, secondShot.shooterCowboyId());
        assertEquals(Direction.LEFT, secondShot.direction());
        assertEquals(1, firstShot.activeCowboyTurnNumber());
        assertEquals(2, secondShot.activeCowboyTurnNumber());
    }

    @Test
    void lethalShotKeepsShooterActiveAndNeverSelfShoots() {
        // With three cowboys and 5-damage rolls, cowboy 1 kills cowboy 2 then fires again.
        ShootoutResult shootoutResult =
                new ShootoutGame(new PredeterminedRandomSource(0, 4, 4, 4, 4, 4, 4, 4)).play(3);
        int firstKillIndex = -1;
        for (int shotIndex = 0; shotIndex < shootoutResult.shots().size(); shotIndex++) {
            if (shootoutResult.shots().get(shotIndex).killed()) {
                firstKillIndex = shotIndex;
                break;
            }
        }
        assertTrue(firstKillIndex >= 0);
        ShotEvent killingShot = shootoutResult.shots().get(firstKillIndex);
        assertEquals(1, killingShot.shooterCowboyId());
        if (firstKillIndex + 1 < shootoutResult.shots().size()) {
            ShotEvent shotAfterKill = shootoutResult.shots().get(firstKillIndex + 1);
            assertEquals(1, shotAfterKill.shooterCowboyId());
            assertEquals(killingShot.activeCowboyTurnNumber(), shotAfterKill.activeCowboyTurnNumber());
        }
        assertTrue(shootoutResult.shots().stream()
                .noneMatch(shot -> shot.shooterCowboyId() == shot.targetCowboyId()));
    }

    @Test
    void clampsOverkillToZeroAndKeepsActualRoll() {
        // Two cowboys: both first lose 1 HP, then cowboy 1 reaches 4 HP and is overkilled.
        ShootoutResult shootoutResult =
                new ShootoutGame(new PredeterminedRandomSource(0, 0, 0, 4, 4, 4)).play(2);
        ShotEvent overkillShot = shootoutResult.shots().stream()
                .filter(shot -> shot.overkill() > 0)
                .findFirst()
                .orElseThrow();
        assertEquals(0, overkillShot.targetHealthPointsAfter());
        assertEquals(overkillShot.targetHealthPointsBefore(), overkillShot.effectiveHealthPointsLost());
        assertEquals(overkillShot.damageRolled() - overkillShot.effectiveHealthPointsLost(), overkillShot.overkill());
    }

    @Test
    void sameSeedProducesSameImmutableProtocol() {
        ShootoutResult firstRun = new ShootoutGame(new JavaRandomSource(123456L)).play(8);
        ShootoutResult secondRun = new ShootoutGame(new JavaRandomSource(123456L)).play(8);
        final List<ShotEvent> immutableShotHistory = firstRun.shots();
        assertEquals(firstRun, secondRun);
        assertThrows(UnsupportedOperationException.class, () -> immutableShotHistory.add(null));
    }

    @Test
    void knownSeedKeepsTheEstablishedRandomSequence() {
        ShootoutResult shootoutResult =
                new ShootoutGame(new JavaRandomSource(123456L)).play(5);

        ShotEvent firstShot = shootoutResult.shots().getFirst();
        assertEquals(3, shootoutResult.startingCowboyId());
        assertEquals(3, firstShot.shooterCowboyId());
        assertEquals(2, firstShot.targetCowboyId());
        assertEquals(3, firstShot.damageRolled());
        assertEquals(16, shootoutResult.shots().size());
        assertEquals(new Winner(3, 2), shootoutResult.winner());
    }

    @Test
    void expressesWinnerPositionAsOffsetAndPercentageOfTheCircle() {
        final WinnerPosition winnerPosition = WinnerPosition.calculate(5, 3, 0);

        assertEquals(2, winnerPosition.offsetFromStarter());
        assertEquals(40.0, winnerPosition.percentageTowardLeft());
        assertEquals(60.0, winnerPosition.percentageTowardRight());

        final WinnerPosition starterWins = WinnerPosition.calculate(5, 3, 3);
        assertEquals(0.0, starterWins.percentageTowardLeft());
        assertEquals(0.0, starterWins.percentageTowardRight());
    }

    @Test
    void statisticsAreConsistentWithTheImmutableShotHistory() {
        ShootoutResult shootoutResult = new ShootoutGame(new JavaRandomSource(2026L)).play(5);
        GameStatistics statistics = shootoutResult.statistics();
        assertEquals(5, statistics.cowboys().size());
        assertEquals(shootoutResult.shots().size(), statistics.cowboys().stream().mapToInt(CowboyStatistics::shotsFired).sum());
        assertEquals(4, statistics.cowboys().stream().mapToInt(CowboyStatistics::kills).sum());
        assertEquals(
                shootoutResult.shots().stream()
                        .mapToInt(ShotEvent::effectiveHealthPointsLost)
                        .sum(),
                statistics.cowboys().stream().mapToInt(CowboyStatistics::totalDamageDealt).sum());
        assertEquals(statistics.cowboys().stream().mapToInt(CowboyStatistics::totalDamageDealt).sum(),
                statistics.cowboys().stream().mapToInt(CowboyStatistics::totalDamageTaken).sum());
    }

    @Test
    void compactSummaryMatchesDetailedResultWithoutExposingShotHistory() {
        ShootoutSetup shootoutSetup = new ShootoutSetup(12, 4);
        ShootoutResult detailedResult =
                new ShootoutGame(new JavaRandomSource(24680L)).play(shootoutSetup);
        ShootoutSummary compactSummary =
                new ShootoutGame(new JavaRandomSource(24680L)).summarize(shootoutSetup);

        assertEquals(detailedResult.numberOfCowboys(), compactSummary.numberOfCowboys());
        assertEquals(detailedResult.startingCowboyId(), compactSummary.startingCowboyId());
        assertEquals(detailedResult.shots().size(), compactSummary.totalShots());
        assertEquals(detailedResult.winner(), compactSummary.winner());
        CowboyStatistics detailedWinnerStatistics = detailedResult.statistics().cowboys()
                .get(detailedResult.winner().cowboyId());
        assertEquals(new WinnerStatistics(detailedWinnerStatistics.shotsFired(), detailedWinnerStatistics.kills(),
                detailedWinnerStatistics.totalDamageDealt()), compactSummary.winnerStatistics());
    }

    private static final class PredeterminedRandomSource implements RandomSource {
        private final Deque<Integer> queuedValues = new ArrayDeque<>();

        PredeterminedRandomSource(int... predeterminedValues) {
            for (int predeterminedValue : predeterminedValues) {
                queuedValues.addLast(predeterminedValue);
            }
        }

        @Override
        public int nextInt(int exclusiveUpperBound) {
            if (queuedValues.isEmpty()) {
                return 0;
            }
            int nextValue = queuedValues.removeFirst();
            if (nextValue < 0 || nextValue >= exclusiveUpperBound) {
                throw new AssertionError("Invalid deterministic random value");
            }
            return nextValue;
        }
    }
}