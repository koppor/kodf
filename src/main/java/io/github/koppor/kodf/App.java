package io.github.koppor.kodf;

import io.github.koppor.kodf.jgraphtsupport.HashableEdge;
import java.nio.file.Path;
import org.jgrapht.Graph;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListenerAdapter;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;
import picocli.CommandLine;

public class App {

  @CommandLine.Option(
      names = {"-h", "--help"},
      usageHelp = true,
      description = "display a help message")
  private boolean usageHelpRequested = false;

  public static void main(String[] args) {
    App app = CommandLine.populateCommand(new App(), args);
    if (app.usageHelpRequested) {
      CommandLine.usage(new App(), System.out);
      return;
    }

    DuplicateChecker duplicateChecker =
        DuplicateChecker.builder()
            .pathToScan(Path.of("C:\\TEMP\\testdup\\dira"))
            .pathToScan(Path.of("C:\\TEMP\\testdup\\dirb"))
            .build();

    duplicateChecker.checkDuplicates();

    Graph<Path, HashableEdge> result = duplicateChecker.getPathRelation();

    outputResult(result);
  }

  private static void outputResult(Graph<Path, HashableEdge> result) {
    System.out.println();
    System.out.println("== Result ==");
    System.out.println();
    GraphIterator<Path, HashableEdge> iterator = new DepthFirstIterator<Path, HashableEdge>(result);
    iterator.addTraversalListener(
        new TraversalListenerAdapter<Path, HashableEdge>() {
          @Override
          public void edgeTraversed(EdgeTraversalEvent<HashableEdge> e) {
            String source = e.getEdge().getSource().toString();
            String target = e.getEdge().getTarget().toString();
            String output = source + " -> " + target;
            // Logger.debug(output);
            System.out.println(output);
          }
        });

    while (iterator.hasNext()) {
      iterator.next();
    }
  }
}
