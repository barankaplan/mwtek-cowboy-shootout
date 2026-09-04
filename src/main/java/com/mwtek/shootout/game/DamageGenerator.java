package com.mwtek.shootout.game;

import com.mwtek.shootout.game.random.RandomSource;
import java.util.Objects;

/** Generates damage values within the range defined by the game rules. */
final class DamageGenerator {
    private final RandomSource randomSource;

    DamageGenerator(RandomSource randomSource) {
        this.randomSource = Objects.requireNonNull(randomSource, "randomSource");
    }

    int nextDamage() {
        return GameRules.MIN_DAMAGE + randomSource.nextInt(GameRules.DAMAGE_OUTCOME_COUNT);
    }
}