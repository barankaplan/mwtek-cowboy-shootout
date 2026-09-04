/**
 * Runs multiple shootout simulations and writes one compact CSV row per game.
 *
 * <p>Batch games use {@code ShootoutSummary}, so detailed shot events are not
 * created or retained. Only the winning cowboy's statistics are copied into
 * each summary. A normal single-game run still produces the complete audit
 * history required by the application and JSON protocol.
 */
package com.mwtek.shootout.batch;