package com.mwtek.shootout.output;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mwtek.shootout.game.ShootoutGame;
import com.mwtek.shootout.game.random.JavaRandomSource;
import com.mwtek.shootout.game.result.ShootoutResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProtocolPipelineTest {
    @TempDir Path temporaryDirectory;

    @Test
    void writesParseableUtf8JsonAndHashesTheActualFile() throws Exception {
        ShootoutResult shootoutResult = new ShootoutGame(new JavaRandomSource(44)).play(4);
        Path protocolFile = temporaryDirectory.resolve("protocol.json");
        new ProtocolWriter().write(shootoutResult, 44L, protocolFile);

        JsonNode protocolJson = new ObjectMapper().readTree(Files.readAllBytes(protocolFile));
        assertEquals(4, protocolJson.path("numberOfCowboys").asInt());
        assertEquals(44L, protocolJson.path("seed").asLong());
        assertTrue(protocolJson.has("shots"));
        assertTrue(protocolJson.has("winner"));
        JsonNode firstShot = protocolJson.path("shots").get(0);
        assertTrue(firstShot.has("turnNumber"));
        assertTrue(firstShot.has("shooterId"));
        assertTrue(firstShot.has("shooterHp"));
        assertTrue(firstShot.has("targetId"));
        assertTrue(firstShot.has("targetHpBefore"));
        assertTrue(firstShot.has("effectiveHpLost"));
        assertTrue(firstShot.has("targetHpAfter"));
        assertTrue(firstShot.has("remainingCowboysAfterShot"));
        assertFalse(firstShot.has("activeCowboyTurnNumber"));
        assertFalse(firstShot.has("shooterCowboyId"));
        assertTrue(protocolJson.path("winner").has("remainingHp"));
        assertFalse(protocolJson.path("winner").has("remainingHealthPoints"));
        String originalChecksum = ChecksumCalculator.sha256(protocolFile);
        Files.writeString(protocolFile, Files.readString(protocolFile) + " ");
        assertNotEquals(originalChecksum, ChecksumCalculator.sha256(protocolFile));
    }

    @Test
    void calculatesTheStandardSha256ValueForKnownBytes() throws Exception {
        Path knownContentFile = temporaryDirectory.resolve("known.txt");
        Files.writeString(knownContentFile, "abc");
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                ChecksumCalculator.sha256(knownContentFile));
    }

    @Test
    void failedAtomicWritePreservesExistingDestinationAndCleansTemporaryFile() throws Exception {
        final Path destination = temporaryDirectory.resolve("existing.json");
        Files.writeString(destination, "original");

        assertThrows(IOException.class, () -> AtomicFileWriter.write(destination, temporaryFile -> {
            Files.writeString(temporaryFile, "partial replacement");
            throw new IOException("simulated write failure");
        }));

        assertEquals("original", Files.readString(destination));
        try (Stream<Path> directoryEntries = Files.list(temporaryDirectory)) {
            assertEquals(1, directoryEntries.count());
        }
    }

    @Test
    void errorDuringAtomicWriteAlsoCleansTemporaryFile() throws Exception {
        final Path destination = temporaryDirectory.resolve("error.json");

        assertThrows(AssertionError.class, () -> AtomicFileWriter.write(destination, temporaryFile -> {
            Files.writeString(temporaryFile, "partial output");
            throw new AssertionError("simulated fatal failure");
        }));

        try (Stream<Path> directoryEntries = Files.list(temporaryDirectory)) {
            assertEquals(0, directoryEntries.count());
        }
    }
}