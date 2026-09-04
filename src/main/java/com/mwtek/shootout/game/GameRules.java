package com.mwtek.shootout.game;

/** Immutable rules of the cowboy shootout. */
public final class GameRules {
    public static final int INITIAL_HEALTH_POINTS = 10;
    public static final int MIN_DAMAGE = 1;
    public static final int MAX_DAMAGE = 5;
    public static final int DAMAGE_OUTCOME_COUNT = MAX_DAMAGE - MIN_DAMAGE + 1;

    private GameRules() {
    }
}