package es.brasatech.fastbite.jpa.tenant;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.logging.Logger;

/** Stage beside the destination, retain the original directory until the database commits. */
final class BackupMediaReplacement implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(BackupMediaReplacement.class.getName());
    private final Path destination;
    private final Path staging;
    private final Path media;
    private final Path previous;
    private boolean movedOriginal;
    private boolean installed;
    private boolean committed;

    BackupMediaReplacement(Path destination) throws IOException {
        this.destination = destination;
        Files.createDirectories(destination.getParent());
        staging = Files.createTempDirectory(destination.getParent(), ".restore-");
        media = Files.createDirectory(staging.resolve("media"));
        previous = staging.resolve("previous");
    }

    Path stagedMedia() { return media; }

    void install() {
        try {
            if (Files.exists(destination)) {
                Files.move(destination, previous);
                movedOriginal = true;
            }
            Files.move(media, destination);
            installed = true;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot replace tenant media", e);
        }
    }

    void committed() { committed = true; }

    @Override
    public void close() throws IOException {
        if (!committed) {
            // If rollback fails, retain staging/previous for recovery; never delete the only copy.
            if (installed) deleteTree(destination);
            if (movedOriginal) Files.move(previous, destination);
        }
        try {
            deleteTree(staging);
        } catch (IOException e) {
            LOG.warning("Backup staging cleanup failed; retained at " + staging);
        }
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) return;
        try (var files = Files.walk(root)) {
            for (Path file : files.sorted(Comparator.reverseOrder()).toList()) Files.delete(file);
        }
    }
}
