package com.mwtek.shootout.game.statistics;

/** Immutable end-of-game totals for one cowboy. */
public record CowboyStatistics(int cowboyId, int shotsFired, int kills, int totalDamageDealt, int hitsTaken, int totalDamageTaken) {
}