package io.github.koppor.kodf.duplication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class DuplicateCheckerTest {

  private final FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

  // TODO rewrite without jimfs because production code uses features of java.nio.path which are not
  // supported by jimfs

  // @Test
  void testProperSubSetWithOneMoreFileHavingSameSize() throws Exception {
    // /x is contained in /y

    // Idea:

    // /x/a.txt
    // /y/a.txt
    // /y/b.txt

    Path dirX = fs.getPath("/x");
    Files.createDirectory(dirX);
    Path dirY = fs.getPath("/y");
    Files.createDirectory(dirY);

    Path aTxtInDirX = dirX.resolve("a.txt");
    Files.write(aTxtInDirX, List.of("content-of-a.txt"), StandardCharsets.UTF_8);

    Path aTxtInDirY = dirY.resolve("a.txt");
    Files.write(aTxtInDirY, List.of("content-of-a.txt"), StandardCharsets.UTF_8);

    Path bTxtInDirY = dirY.resolve("b.txt");
    Files.write(bTxtInDirY, List.of("content-of-b.txt"), StandardCharsets.UTF_8);

    DuplicateCheckerConfig config =
        DuplicateCheckerConfig.builder().pathToScan(dirX).pathToScan(dirY).build();
    DuplicateChecker duplicateChecker = new DuplicateChecker(config);
    duplicateChecker.run();

    assertTrue(duplicateChecker.getKnownSuperSets().containsMapping(dirX, dirY));
    assertEquals(1, duplicateChecker.getKnownSuperSets().size());
  }
}
