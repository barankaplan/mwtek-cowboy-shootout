package com.mwtek.shootout.game;

import com.mwtek.shootout.game.result.Winner;

/**
 * Holds the mutable state of one shootout.
 *
 * <p>Array indices are stable cowboy IDs. For example, {@code cowboys[2]} is
 * Cowboy 2 while alive and becomes {@code null} after elimination. Other
 * cowboys never move to a different index. Each living Cowboy object holds its
 * own health and the IDs of its living left and right neighbours.
 */
final class ShootoutState {
    private final Cowboy[] cowboys;
    private int currentCowboyId;
    private int remainingCowboyCount;
    private int activeCowboyTurnNumber = 1;

    ShootoutState(ShootoutSetup shootoutSetup) {
        cowboys = new Cowboy[shootoutSetup.numberOfCowboys()];
        initializeCowboys(shootoutSetup.numberOfCowboys());
        currentCowboyId = shootoutSetup.startingCowboyId();
        remainingCowboyCount = shootoutSetup.numberOfCowboys();
    }

    int getCurrentCowboyId() {
        return currentCowboyId;
    }

    int getHealthPoints(int cowboyId) {
        return requireLivingCowboy(cowboyId).getHealthPoints();
    }

    /**
     * Counts changes of the active cowboy, not shots. A cowboy that kills a target
     * remains active, so consecutive shots can have the same turn number.
     */
    int getActiveCowboyTurnNumber() {
        return activeCowboyTurnNumber;
    }

    int getRemainingCowboyCount() {
        return remainingCowboyCount;
    }

    boolean isComplete() {
        return remainingCowboyCount == 1;
    }

    boolean isCowboyAlive(int cowboyId) {
        return isValidCowboyId(cowboyId)
                && cowboys[cowboyId] != null
                && cowboys[cowboyId].isAlive();
    }

    int getNeighbor(int cowboyId, Direction direction) {
        return requireLivingCowboy(cowboyId).getNeighborCowboyId(direction);
    }

    DamageOutcome applyDamageToCowboy(int targetCowboyId, int damageRolled) {
        final Cowboy targetCowboy = requireLivingCowboy(targetCowboyId);
        if (remainingCowboyCount == 1) {
            throw new IllegalStateException("The sole survivor cannot be targeted");
        }
        validateDamage(damageRolled);
        return targetCowboy.takeDamage(damageRolled);
    }

    void eliminateCowboy(int targetCowboyId) {
        final Cowboy targetCowboy = requireLivingCowboy(targetCowboyId);
        if (targetCowboy.getHealthPoints() > 0) {
            throw new IllegalStateException("A living cowboy cannot be eliminated");
        }
        final int leftNeighborId = targetCowboy.getNeighborCowboyId(Direction.LEFT);
        final int rightNeighborId = targetCowboy.getNeighborCowboyId(Direction.RIGHT);
        final Cowboy leftNeighbor = requireLivingCowboy(leftNeighborId);
        final Cowboy rightNeighbor = requireLivingCowboy(rightNeighborId);

        leftNeighbor.changeRightNeighborTo(rightNeighborId);
        rightNeighbor.changeLeftNeighborTo(leftNeighborId);
        targetCowboy.markAsEliminated();
        remainingCowboyCount--;
        cowboys[targetCowboyId] = null;
    }

    void passTurnToCowboy(int nextCowboyId) {
        requireLivingCowboy(nextCowboyId);
        currentCowboyId = nextCowboyId;
        activeCowboyTurnNumber++;
    }

    Winner getWinner() {
        if (!isComplete()) {
            throw new IllegalStateException("The shootout is not complete");
        }
        final Cowboy winner = requireLivingCowboy(currentCowboyId);
        return new Winner(winner.getCowboyId(), winner.getHealthPoints());
    }

    private void initializeCowboys(int numberOfCowboys) {
        for (int cowboyId = 0; cowboyId < numberOfCowboys; cowboyId++) {
            final int rightNeighborCowboyId = Math.floorMod(cowboyId - 1, numberOfCowboys);
            final int leftNeighborCowboyId = (cowboyId + 1) % numberOfCowboys;
            cowboys[cowboyId] = new Cowboy(
                    cowboyId,
                    GameRules.INITIAL_HEALTH_POINTS,
                    rightNeighborCowboyId,
                    leftNeighborCowboyId);
        }
    }

    private Cowboy requireLivingCowboy(int cowboyId) {
        if (!isCowboyAlive(cowboyId)) {
            throw new IllegalArgumentException("Cowboy " + cowboyId + " is not alive");
        }
        return cowboys[cowboyId];
    }

    private boolean isValidCowboyId(int cowboyId) {
        return cowboyId >= 0 && cowboyId < cowboys.length;
    }

    private static void validateDamage(int damageRolled) {
        if (damageRolled < GameRules.MIN_DAMAGE || damageRolled > GameRules.MAX_DAMAGE) {
            throw new IllegalArgumentException("Damage is outside the configured range");
        }
    }
}