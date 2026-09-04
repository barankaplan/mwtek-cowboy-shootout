package com.mwtek.shootout.game.statistics;

import java.util.stream.IntStream;

/** Mutable O(1) counters used only while a game is running. */
public final class StatisticsCollector {
    private final int[] shotsFiredByCowboy;
    private final int[] killsByCowboy;
    private final int[] damageDealtByCowboy;
    private final int[] hitsTakenByCowboy;
    private final int[] damageTakenByCowboy;

    public StatisticsCollector(int numberOfCowboys) {
        shotsFiredByCowboy = new int[numberOfCowboys];
        killsByCowboy = new int[numberOfCowboys];
        damageDealtByCowboy = new int[numberOfCowboys];
        hitsTakenByCowboy = new int[numberOfCowboys];
        damageTakenByCowboy = new int[numberOfCowboys];
    }

    public void recordShot(int shooterCowboyId, int targetCowboyId, int effectiveHealthLost, boolean killed) {
        shotsFiredByCowboy[shooterCowboyId]++;
        damageDealtByCowboy[shooterCowboyId] += effectiveHealthLost;
        hitsTakenByCowboy[targetCowboyId]++;
        damageTakenByCowboy[targetCowboyId] += effectiveHealthLost;
        if (killed) {
            killsByCowboy[shooterCowboyId]++;
        }
    }

    public GameStatistics snapshot() {
        return new GameStatistics(IntStream.range(0, shotsFiredByCowboy.length)
                .mapToObj(cowboyId -> new CowboyStatistics(cowboyId, shotsFiredByCowboy[cowboyId], killsByCowboy[cowboyId],
                        damageDealtByCowboy[cowboyId], hitsTakenByCowboy[cowboyId], damageTakenByCowboy[cowboyId]))
                .toList());
    }

    public WinnerStatistics snapshotForCowboy(int cowboyId) {
        if (cowboyId < 0 || cowboyId >= shotsFiredByCowboy.length) {
            throw new IllegalArgumentException("Cowboy ID is outside the statistics collection");
        }
        return new WinnerStatistics(shotsFiredByCowboy[cowboyId], killsByCowboy[cowboyId], damageDealtByCowboy[cowboyId]);
    }
}