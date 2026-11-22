package io.github.koppor.kodf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SubsetsTest {

  @TempDir Path tmp;

  private void write(String path, String content) throws Exception {
    Path file = tmp.resolve(path);
    Files.createDirectories(file.getParent());
    Files.writeString(file, content);
  }

  private Map<Path, List<Path>> scan() throws Exception {
    return scan(1);
  }

  private Map<Path, List<Path>> scan(long minSize) throws Exception {
    try (Connection db = Db.open(tmp.resolve("kodf.db"))) {
      new Scanner(db, minSize).scan(List.of(tmp.resolve("data")));
      return Subsets.find(db);
    }
  }

  // [utest->dsn~sqlite-index~1]
  // [utest->dsn~subset-anywhere~1]
  // [utest->dsn~outermost-subset-smallest-superset~1]
  @Test
  void findsSubsetInUnrelatedSubtree() throws Exception {
    write("data/backups/2019-01-05/white-hdd/a.txt", "aaa");
    write("data/backups/2019-01-05/white-hdd/sub/b.txt", "bbb");
    write("data/to-sort/whiteone/a-renamed.txt", "aaa");
    write("data/to-sort/whiteone/other/b.txt", "bbb");
    write("data/to-sort/whiteone/c.txt", "ccc");

    assertEquals(
        Map.of(
            tmp.resolve("data/backups"),
            List.of(tmp.resolve("data/to-sort/whiteone")),
            // The other direction holds for a subdirectory, which lies somewhere else.
            tmp.resolve("data/to-sort/whiteone/other"),
            List.of(tmp.resolve("data/backups/2019-01-05/white-hdd/sub"))),
        scan());
  }

  // [utest->dsn~hash-size-collisions~1]
  @Test
  void fileWithUniqueContentPreventsSubset() throws Exception {
    write("data/a/x.txt", "xxx");
    write("data/a/only-here.txt", "yyy");
    write("data/b/x.txt", "xxx");
    write("data/b/z.txt", "zzz");

    assertEquals(Map.of(), scan());
  }

  // [utest->dsn~incremental-rescan~1]
  @Test
  void rescanForgetsDeletedFiles() throws Exception {
    write("data/a/x.txt", "xxx");
    write("data/a/y.txt", "yyyy");
    write("data/b/x.txt", "xxx");
    write("data/b/y.txt", "yyyy");
    write("data/b/extra.txt", "e");
    assertEquals(Map.of(tmp.resolve("data/a"), List.of(tmp.resolve("data/b"))), scan());

    Files.delete(tmp.resolve("data/b/y.txt"));
    assertEquals(Map.of(), scan());
  }

  // [utest->dsn~min-size~1]
  @Test
  void smallFilesAreLeftOut() throws Exception {
    write("data/a/big.bin", "bigbig");
    write("data/a/small.txt", "s");
    write("data/b/big.bin", "bigbig");
    write("data/b/small.txt", "t");
    Path a = tmp.resolve("data/a");
    Path b = tmp.resolve("data/b");

    assertEquals(Map.of(), scan(1));
    assertEquals(Map.of(a, List.of(b), b, List.of(a)), scan(2));
  }
}
