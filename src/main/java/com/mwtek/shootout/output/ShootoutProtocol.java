package com.mwtek.shootout.output;

import com.mwtek.shootout.game.GameRules;
import com.mwtek.shootout.game.result.ShootoutResult;
import com.mwtek.shootout.game.result.ShotEvent;
import com.mwtek.shootout.game.result.Winner;
import com.mwtek.shootout.game.statistics.GameStatistics;
import java.util.List;

/** JSON-facing representation of one completed cowboy shootout. */
record ShootoutProtocol(int numberOfCowboys, int initialHealthPoints, DamageRange damageRange, long seed, int startingCowboy, List<ShotEvent> shots,
        ProtocolWinner winner, GameStatistics statistics) {
    record DamageRange(int min, int max) {
    }

    record ProtocolWinner(int cowboyId, int remainingHp) {
        static ProtocolWinner from(Winner winner) {
            return new ProtocolWinner(winner.cowboyId(), winner.remainingHealthPoints());
        }
    }

    static ShootoutProtocol fromShootoutResult(ShootoutResult shootoutResult, long randomSeed) {
        return new ShootoutProtocol(shootoutResult.numberOfCowboys(), GameRules.INITIAL_HEALTH_POINTS,
                new DamageRange(GameRules.MIN_DAMAGE, GameRules.MAX_DAMAGE), randomSeed, shootoutResult.startingCowboyId(),
                shootoutResult.shots(), ProtocolWinner.from(shootoutResult.winner()), shootoutResult.statistics());
    }
}