package io.github.koppor.kodf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Runs only with {@code KODF_BTRFS_DIR} naming a writable directory on btrfs, see {@code test-on-btrfs.sh}. */
class BtrfsTest {

    Path tmp;

    @BeforeEach
    void createDirectory() throws Exception {
        String dir = System.getenv("KODF_BTRFS_DIR");
        assumeTrue(dir != null, "KODF_BTRFS_DIR is not set");
        tmp = Files.createTempDirectory(Path.of(dir), "kodf");
    }

    private Path write(String path, byte[] content) throws Exception {
        Path file = tmp.resolve(path);
        Files.createDirectories(file.getParent());
        return Files.write(file, content);
    }

    private Path reflink(Path from, String to) throws Exception {
        Path file = tmp.resolve(to);
        Files.createDirectories(file.getParent());
        Process cp = new ProcessBuilder("cp", "--reflink=always", from.toString(), file.toString())
                .inheritIO()
                .start();
        assertEquals(0, cp.waitFor());
        return file;
    }

    private static byte[] random(int size) {
        byte[] bytes = new byte[size];
        new Random(size).nextBytes(bytes);
        return bytes;
    }

    // [utest->dsn~extent-fingerprint~1]
    @Test
    void reflinkSharesDataButCopyDoesNot() throws Exception {
        byte[] content = random(1 << 20);
        Path original = write("a", content);
        Path copy = write("b", content);

        String fingerprint = Extents.fingerprint(original);
        assertNotNull(fingerprint);
        assertEquals(fingerprint, Extents.fingerprint(reflink(original, "c")));
        assertNotEquals(fingerprint, Extents.fingerprint(copy));
    }

    // [utest->dsn~shared-data-not-read~1]
    // [utest->dsn~shared-data-report~1]
    @Test
    void reportTellsWhetherDataIsShared() throws Exception {
        Path a = write("data/photos/a.jpg", random(200_000));
        Path b = write("data/photos/b.jpg", random(300_000));
        reflink(a, "data/reflinked/a.jpg");
        reflink(b, "data/reflinked/b.jpg");
        write("data/copied/a.jpg", Files.readAllBytes(a));

        try (Connection db = Db.open(tmp.resolve("kodf.db"))) {
            new Scanner(db, 1).scan(List.of(tmp.resolve("data")));
            Map<Path, List<Path>> subsets = Subsets.find(db);
            Path photos = tmp.resolve("data/photos");
            Path reflinked = tmp.resolve("data/reflinked");
            Path copied = tmp.resolve("data/copied");
            assertEquals(List.of(photos, reflinked), subsets.get(copied));
            assertTrue(Subsets.sharesData(db, reflinked, photos));
            assertFalse(Subsets.sharesData(db, copied, photos));
        }
    }
}
