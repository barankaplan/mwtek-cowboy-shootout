package com.mwtek.shootout.game.random;

/** Supplies random values for starter selection and damage rolls. */
@FunctionalInterface
public interface RandomSource {
    int nextInt(int exclusiveUpperBound);
}