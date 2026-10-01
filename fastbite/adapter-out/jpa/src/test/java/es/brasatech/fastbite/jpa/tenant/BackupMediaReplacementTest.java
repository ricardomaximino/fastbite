package es.brasatech.fastbite.jpa.tenant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class BackupMediaReplacementTest {
    @TempDir Path root;

    @Test void databaseCommitFailureRestoresTheOriginalDirectory() throws Exception {
        Path target = Files.createDirectory(root.resolve("tenant"));
        Files.writeString(target.resolve("image.txt"), "original");
        try (var replacement = new BackupMediaReplacement(target)) {
            Files.writeString(replacement.stagedMedia().resolve("image.txt"), "new");
            replacement.install();
            assertEquals("new", Files.readString(target.resolve("image.txt")));
            // No committed() call: equivalent to transaction commit throwing after media installation.
        }
        assertEquals("original", Files.readString(target.resolve("image.txt")));
        try (var files = Files.list(root)) { assertEquals(1, files.count()); }
    }
}
