package com.mwtek.shootout.game.random;

import java.util.Random;

/** Random source backed by {@link Random} with an explicit reproducible seed. */
public final class JavaRandomSource implements RandomSource {
    private final Random randomNumberGenerator;

    public JavaRandomSource(long seed) {
        randomNumberGenerator = new Random(seed);
    }

    @Override
    public int nextInt(int exclusiveUpperBound) {
        return randomNumberGenerator.nextInt(exclusiveUpperBound);
    }
}