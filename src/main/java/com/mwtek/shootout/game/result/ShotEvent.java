package com.mwtek.shootout.game.result;

import com.mwtek.shootout.game.Direction;
import java.util.Objects;

/**
 * Immutable audit snapshot; it never retains a mutable game or cowboy reference.
 * The active-cowboy turn changes only when a surviving target becomes the next shooter.
 */
public record ShotEvent(
        int shotNumber,
        int activeCowboyTurnNumber,
        int shooterCowboyId,
        int shooterHealthPoints,
        Direction direction,
        int targetCowboyId,
        int targetHealthPointsBefore,
        int damageRolled,
        int effectiveHealthPointsLost,
        int targetHealthPointsAfter,
        int overkill,
        boolean killed,
        int remainingCowboyCountAfterShot) {
    public ShotEvent {
        Objects.requireNonNull(direction, "direction");
    }
}