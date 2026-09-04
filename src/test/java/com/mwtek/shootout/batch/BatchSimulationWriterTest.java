package com.mwtek.shootout.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.OptionalInt;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BatchSimulationWriterTest {
    @TempDir Path temporaryDirectory;

    @Test
    void writesOneDeterministicSummaryRowPerGame() throws Exception {
        Path summaryFile = temporaryDirectory.resolve("summary.csv");
        BatchSimulationPlan plan = new BatchSimulationPlan(
                4, 12, OptionalLong.of(77L), OptionalInt.of(0));
        new BatchSimulationWriter().write(plan, summaryFile);
        List<String> summaryRows = Files.readAllLines(summaryFile);
        assertEquals(13, summaryRows.size());
        assertTrue(summaryRows.getFirst().contains("winnerShotsFired"));
        for (String summaryRow : summaryRows.subList(1, summaryRows.size())) {
            String[] summaryColumns = summaryRow.split(",");
            assertEquals("0", summaryColumns[3]);
            assertTrue(Integer.parseInt(summaryColumns[5]) >= 0
                    && Integer.parseInt(summaryColumns[5]) < 4);
            assertTrue(Integer.parseInt(summaryColumns[8]) >= 0);
        }
    }
}