package com.mwtek.shootout.game.statistics;

import java.util.List;
import java.util.Objects;

/** Immutable per-cowboy statistics for a completed shootout. */
public record GameStatistics(List<CowboyStatistics> cowboys) {
    public GameStatistics {
        cowboys = List.copyOf(Objects.requireNonNull(cowboys, "cowboys"));
    }
}