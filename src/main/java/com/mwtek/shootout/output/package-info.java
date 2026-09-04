/**
 * Presents completed game information outside the core game.
 *
 * <p>Start with {@code ProtocolWriter} to find where the archived JSON is
 * written and {@code ShootoutProtocol} to see its structure. The
 * {@code ChecksumCalculator} calculates its SHA-256 checksum, while
 * {@code ConsoleReporter} only presents results in the terminal. Files are
 * completed through {@code AtomicFileWriter}. No game decisions or state
 * changes belong in this package.
 */
package com.mwtek.shootout.output;