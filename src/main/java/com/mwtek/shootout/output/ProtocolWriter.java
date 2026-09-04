package com.mwtek.shootout.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mwtek.shootout.game.result.ShootoutResult;
import com.mwtek.shootout.game.result.ShotEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/** Writes the detailed JSON protocol for a completed shootout. */
public final class ProtocolWriter {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .addMixIn(ShotEvent.class, ProtocolShotMixin.class);

    public void write(ShootoutResult shootoutResult, long randomSeed, Path protocolOutputPath) throws IOException {
        Objects.requireNonNull(shootoutResult, "shootoutResult");
        Objects.requireNonNull(protocolOutputPath, "protocolOutputPath");
        final ShootoutProtocol protocol = ShootoutProtocol.fromShootoutResult(shootoutResult, randomSeed);
        AtomicFileWriter.write(protocolOutputPath, temporaryFile -> writeUtf8Json(temporaryFile, protocol));
    }

    private static void writeUtf8Json(Path temporaryFile, ShootoutProtocol protocol) throws IOException {
        try (BufferedWriter jsonWriter = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8)) {
            MAPPER.writeValue(jsonWriter, protocol);
        }
    }

    /** Preserves the established JSON field names without copying every shot. */
    private abstract static class ProtocolShotMixin {
        @JsonProperty("turnNumber")
        abstract int activeCowboyTurnNumber();

        @JsonProperty("shooterId")
        abstract int shooterCowboyId();

        @JsonProperty("shooterHp")
        abstract int shooterHealthPoints();

        @JsonProperty("targetId")
        abstract int targetCowboyId();

        @JsonProperty("targetHpBefore")
        abstract int targetHealthPointsBefore();

        @JsonProperty("effectiveHpLost")
        abstract int effectiveHealthPointsLost();

        @JsonProperty("targetHpAfter")
        abstract int targetHealthPointsAfter();

        @JsonProperty("remainingCowboysAfterShot")
        abstract int remainingCowboyCountAfterShot();
    }
}