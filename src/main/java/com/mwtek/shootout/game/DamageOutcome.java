package com.mwtek.shootout.game;

/** Immutable result of applying one valid damage roll to a cowboy. */
record DamageOutcome(int healthBefore, int rolledDamage, int effectiveHealthLost, int healthAfter, int overkill, boolean killed) {
}