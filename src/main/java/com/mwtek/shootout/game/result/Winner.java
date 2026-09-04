package com.mwtek.shootout.game.result;

/** Identity and remaining health of the last living cowboy. */
public record Winner(int cowboyId, int remainingHealthPoints) {
}