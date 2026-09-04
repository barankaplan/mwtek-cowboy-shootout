package com.mwtek.shootout.game;

/**
 * Holds the mutable state that belongs to one cowboy during a shootout.
 */
final class Cowboy {
    private final int cowboyId;
    private int healthPoints;
    private int rightNeighborCowboyId;
    private int leftNeighborCowboyId;
    private boolean alive = true;

    Cowboy(int cowboyId, int initialHealthPoints, int rightNeighborCowboyId, int leftNeighborCowboyId) {
        this.cowboyId = cowboyId;
        this.healthPoints = initialHealthPoints;
        this.rightNeighborCowboyId = rightNeighborCowboyId;
        this.leftNeighborCowboyId = leftNeighborCowboyId;
    }

    int getCowboyId() {
        return cowboyId;
    }

    int getHealthPoints() {
        return healthPoints;
    }

    int getNeighborCowboyId(Direction direction) {
        return switch (direction) {
            case RIGHT -> rightNeighborCowboyId;
            case LEFT -> leftNeighborCowboyId;
        };
    }

    boolean isAlive() {
        return alive;
    }

    DamageOutcome takeDamage(int damageRolled) {
        final int healthPointsBefore = healthPoints;
        final int healthPointsAfter = Math.max(0, healthPointsBefore - damageRolled);
        final int effectiveHealthLost = healthPointsBefore - healthPointsAfter;
        healthPoints = healthPointsAfter;

        return new DamageOutcome(healthPointsBefore, damageRolled, effectiveHealthLost, healthPointsAfter,
                damageRolled - effectiveHealthLost, healthPointsAfter == 0);
    }

    void changeRightNeighborTo(int cowboyId) {
        rightNeighborCowboyId = cowboyId;
    }

    void changeLeftNeighborTo(int cowboyId) {
        leftNeighborCowboyId = cowboyId;
    }

    void markAsEliminated() {
        alive = false;
    }
}