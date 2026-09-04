package com.mwtek.shootout.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class ShootoutStateTest {
    @Test
    void rejectsInvalidShootoutSetup() {
        assertThrows(IllegalArgumentException.class, () -> new ShootoutSetup(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new ShootoutSetup(3, -1));
        assertThrows(IllegalArgumentException.class, () -> new ShootoutSetup(3, 3));
    }

    @Test
    void connectsBothEndsAndRewiresNeighborsAfterElimination() {
        ShootoutState shootoutState = new ShootoutState(new ShootoutSetup(5, 0));
        assertEquals(1, shootoutState.getNeighbor(0, Direction.LEFT));
        assertEquals(3, shootoutState.getNeighbor(4, Direction.RIGHT));

        shootoutState.applyDamageToCowboy(2, 5);
        shootoutState.applyDamageToCowboy(2, 5);
        shootoutState.eliminateCowboy(2);

        assertEquals(1, shootoutState.getNeighbor(3, Direction.RIGHT));
        assertEquals(3, shootoutState.getNeighbor(1, Direction.LEFT));
        assertFalse(shootoutState.isCowboyAlive(2));
        assertEquals(4, shootoutState.getRemainingCowboyCount());
    }

    @Test
    void releasesEliminatedCowboySlotWithoutCompactingTheArray() throws ReflectiveOperationException {
        final ShootoutState shootoutState = new ShootoutState(new ShootoutSetup(5, 0));
        shootoutState.applyDamageToCowboy(2, 5);
        shootoutState.applyDamageToCowboy(2, 5);
        shootoutState.eliminateCowboy(2);

        final Field cowboysField = ShootoutState.class.getDeclaredField("cowboys");
        cowboysField.setAccessible(true);
        final Cowboy[] cowboySlots = (Cowboy[]) cowboysField.get(shootoutState);

        assertEquals(5, cowboySlots.length);
        assertNull(cowboySlots[2]);
        assertEquals(3, cowboySlots[3].getCowboyId());
    }

    @Test
    void accessingEliminatedCowboyProducesControlledErrors() {
        final ShootoutState shootoutState = new ShootoutState(new ShootoutSetup(3, 0));
        shootoutState.applyDamageToCowboy(1, 5);
        shootoutState.applyDamageToCowboy(1, 5);
        shootoutState.eliminateCowboy(1);

        final IllegalArgumentException healthError = assertThrows(
                IllegalArgumentException.class, () -> shootoutState.getHealthPoints(1));
        final IllegalArgumentException neighborError = assertThrows(
                IllegalArgumentException.class,
                () -> shootoutState.getNeighbor(1, Direction.LEFT));

        assertEquals("Cowboy 1 is not alive", healthError.getMessage());
        assertEquals("Cowboy 1 is not alive", neighborError.getMessage());
    }

    @Test
    void twoCowboyShootoutLeavesSurvivorLinkedToItself() {
        ShootoutState shootoutState = new ShootoutState(new ShootoutSetup(2, 0));
        shootoutState.applyDamageToCowboy(1, 5);
        shootoutState.applyDamageToCowboy(1, 5);
        shootoutState.eliminateCowboy(1);

        assertEquals(0, shootoutState.getWinner().cowboyId());
        assertEquals(0, shootoutState.getNeighbor(0, Direction.LEFT));
        assertEquals(0, shootoutState.getNeighbor(0, Direction.RIGHT));
    }

    @Test
    void calculatesEffectiveDamageAndOverkill() {
        ShootoutState shootoutState = new ShootoutState(new ShootoutSetup(2, 0));
        shootoutState.applyDamageToCowboy(1, 3);
        shootoutState.applyDamageToCowboy(1, 3);

        DamageOutcome lethalDamage = shootoutState.applyDamageToCowboy(1, 5);

        assertEquals(4, lethalDamage.healthBefore());
        assertEquals(4, lethalDamage.effectiveHealthLost());
        assertEquals(1, lethalDamage.overkill());
        assertEquals(0, shootoutState.getHealthPoints(1));
    }

    @Test
    void soleSurvivorCannotBeDamaged() {
        ShootoutState shootoutState = new ShootoutState(new ShootoutSetup(1, 0));

        assertThrows(IllegalStateException.class, () -> shootoutState.applyDamageToCowboy(0, 1));
        assertEquals(GameRules.INITIAL_HEALTH_POINTS, shootoutState.getHealthPoints(0));
    }
}