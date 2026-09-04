/**
 * Contains the complete core game model and runs one shootout.
 *
 * <p>To understand one shot, read {@code ShootoutGame}, {@code ShootoutState},
 * and {@code Cowboy}, in that order. {@code ShootoutGame} decides what happens
 * next, {@code ShootoutState} owns the array-backed circle and current turn,
 * and {@code Cowboy} holds one cowboy's health and neighbour IDs.
 *
 * <p>{@code ShootoutSetup} describes how a game starts. Immutable outputs are
 * under {@code game.result}, counters are under {@code game.statistics}, and
 * random-number implementations are under {@code game.random}. A cowboy's
 * stable integer ID is also its array position. Eliminating a cowboy clears
 * only that slot; the array is never compacted and surviving IDs never change.
 */
package com.mwtek.shootout.game;