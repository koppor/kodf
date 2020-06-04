package io.github.koppor.kodf;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import io.github.koppor.kodf.jgraphtsupport.HashableEdge;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.junit.jupiter.api.Test;

class DuplicateCheckerTest {

  private FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

  @Test
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

    DuplicateChecker duplicateChecker =
        DuplicateChecker.builder().pathToScan(dirX).pathToScan(dirY).build();
    duplicateChecker.checkDuplicates();

    Graph expected = new DefaultDirectedGraph(HashableEdge.class);
    expected.addVertex(dirX);
    expected.addVertex(dirY);
    expected.addEdge(dirY, dirX);

    assertEquals(expected, duplicateChecker.getPathRelation());
  }
}
