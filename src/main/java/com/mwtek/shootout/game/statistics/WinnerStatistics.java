package com.mwtek.shootout.game.statistics;

/** Compact winner-only statistics retained by batch simulations. */
public record WinnerStatistics(
        int shotsFired,
        int kills,
        int totalDamageDealt) {
}