package com.mwtek.shootout.output;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/** Calculates the SHA-256 fingerprint of a completed output file. */
public final class ChecksumCalculator {
    private static final int BUFFER_SIZE = 8 * 1024;
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    private ChecksumCalculator() {
    }

    public static String sha256(final Path filePath) throws IOException {
        Objects.requireNonNull(filePath, "filePath");
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
            updateDigestFromFile(messageDigest, filePath);
            final byte[] digest = messageDigest.digest();
            return HEX_FORMAT.formatHex(digest);
        } catch (NoSuchAlgorithmException unavailableAlgorithmException) {
            throw new IllegalStateException("SHA-256 is unavailable", unavailableAlgorithmException);
        }
    }

    private static void updateDigestFromFile(MessageDigest messageDigest, Path filePath) throws IOException {
        final byte[] buffer = new byte[BUFFER_SIZE];
        try (final InputStream inputStream = Files.newInputStream(filePath)) {
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                messageDigest.update(buffer, 0, bytesRead);
            }
        }
    }
}