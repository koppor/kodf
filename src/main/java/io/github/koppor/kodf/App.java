package io.github.koppor.kodf;

import java.nio.file.Path;
import java.util.stream.Collectors;
import org.eclipse.collections.api.factory.Sets;
import org.tinylog.Logger;
import picocli.CommandLine;

public class App {

  @CommandLine.Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
  private boolean usageHelpRequested = false;

  public static void main(String[] args) {
    App app = CommandLine.populateCommand(new App(), args);
    if (app.usageHelpRequested) {
      CommandLine.usage(new App(), System.out);
      return;
    }

    /*
    DuplicateChecker duplicateChecker =
        DuplicateChecker.builder()
            .pathsToScan(
                Sets.immutable.of(Path.of("C:\\git-repositories\\MyLibreLab")))
            .pathsToIgnore(
                Sets.immutable.of(
                    Path.of("/volume1/homes/alina/#recycle"),
                    Path.of("/volume1/homes/photo/#recycle")))
            .build();

    duplicateChecker.checkDuplicates();

    duplicateChecker
        .getPathSubSetOf()
        .keyBag()
        .toSortedList()
        .forEach(
            path -> {
              StringBuilder stringBuilder = new StringBuilder();
              stringBuilder.append(path.toString());
              stringBuilder.append(" -> ");
              String superPaths =
                  duplicateChecker
                      .getPathSubSetOf()
                      .get(path)
                      .stream()
                      .map(Path::toString)
                      .collect(Collectors.joining(", "));
              stringBuilder.append(superPaths);
              String output = stringBuilder.toString();
              Logger.debug("Superdirs: {}", output);
              System.out.println(output);
            });

     */
  }
}
