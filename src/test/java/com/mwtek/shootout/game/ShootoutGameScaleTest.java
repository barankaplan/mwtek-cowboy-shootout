package com.mwtek.shootout.game;

import static org.junit.jupiter.api.Assertions.assertTrue;
import com.mwtek.shootout.game.random.JavaRandomSource;
import com.mwtek.shootout.game.result.ShootoutResult;
import org.junit.jupiter.api.Test;

class ShootoutGameScaleTest {
    @Test
    void oneThousandCowboysTerminatesWithinTheProvenShotBound() {
        int numberOfCowboys = 1_000;
        ShootoutResult shootoutResult =
                new ShootoutGame(new JavaRandomSource(987654321L)).play(numberOfCowboys);

        assertTrue(shootoutResult.shots().size() <= 10 * numberOfCowboys - 1);
        assertTrue(shootoutResult.winner().cowboyId() >= 0
                && shootoutResult.winner().cowboyId() < numberOfCowboys);
        assertTrue(shootoutResult.shots().stream()
                .noneMatch(shot -> shot.shooterCowboyId() == shot.targetCowboyId()));
    }
}