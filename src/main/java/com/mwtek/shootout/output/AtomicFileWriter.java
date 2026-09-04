package com.mwtek.shootout.output;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/** Writes a complete temporary file before replacing the requested destination. */
public final class AtomicFileWriter {
    private AtomicFileWriter() {
    }

    public static void write(Path destination, ContentWriter contentWriter) throws IOException {
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(contentWriter, "contentWriter");

        final Path absoluteDestination = destination.toAbsolutePath();
        final Path fileName = absoluteDestination.getFileName();
        if (fileName == null) {
            throw new IllegalArgumentException("Output path must name a file");
        }
        final Path outputDirectory = absoluteDestination.getParent();
        Files.createDirectories(outputDirectory);

        try (TemporaryOutputFile temporaryOutputFile =
                TemporaryOutputFile.createIn(outputDirectory, fileName)) {
            contentWriter.write(temporaryOutputFile.path());
            moveIntoPlace(temporaryOutputFile.path(), absoluteDestination);
            temporaryOutputFile.markAsMovedIntoPlace();
        }
    }

    private static void moveIntoPlace(Path temporaryFile, Path destination) throws IOException {
        try {
            Files.move(temporaryFile, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException unsupportedAtomicMove) {
            Files.move(temporaryFile, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static final class TemporaryOutputFile implements AutoCloseable {
        private final Path path;
        private boolean movedIntoPlace;

        private TemporaryOutputFile(Path path) {
            this.path = path;
        }

        static TemporaryOutputFile createIn(Path outputDirectory, Path destinationFileName)
                throws IOException {
            final String temporaryFilePrefix = "." + destinationFileName + ".";
            final Path path = Files.createTempFile(outputDirectory, temporaryFilePrefix, ".tmp");
            return new TemporaryOutputFile(path);
        }

        Path path() {
            return path;
        }

        void markAsMovedIntoPlace() {
            movedIntoPlace = true;
        }

        @Override
        public void close() throws IOException {
            if (!movedIntoPlace) {
                Files.deleteIfExists(path);
            }
        }
    }

    @FunctionalInterface
    public interface ContentWriter {
        void write(Path temporaryFile) throws IOException;
    }
}