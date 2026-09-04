package com.mwtek.shootout.game.result;

import com.mwtek.shootout.game.ShootoutLimits;

/**
 * Describes the winner's position relative to the starting cowboy.
 * Offset zero is the starter; positive offsets follow cowboy IDs toward LEFT.
 */
public record WinnerPosition(int offsetFromStarter, int numberOfCowboys) {
    public WinnerPosition {
        ShootoutLimits.validatePositiveCowboyCount(numberOfCowboys);
        if (offsetFromStarter < 0 || offsetFromStarter >= numberOfCowboys) {
            throw new IllegalArgumentException("Winner offset must be inside the circle");
        }
    }

    public static WinnerPosition calculate(int numberOfCowboys, int startingCowboyId, int winnerCowboyId) {
        final int offsetFromStarter = Math.floorMod(winnerCowboyId - startingCowboyId, numberOfCowboys);
        return new WinnerPosition(offsetFromStarter, numberOfCowboys);
    }

    public double percentageTowardLeft() {
        return 100.0 * offsetFromStarter / numberOfCowboys;
    }

    public double percentageTowardRight() {
        return offsetFromStarter == 0 ? 0.0 : 100.0 - percentageTowardLeft();
    }
}