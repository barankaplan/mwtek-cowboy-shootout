package com.mwtek.shootout.cli;

/** A validated command that the application can execute. */
public sealed interface CliCommand permits SingleGameCommand, BatchSimulationCommand {
}