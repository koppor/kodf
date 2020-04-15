package io.github.koppor.kodf;

import java.nio.file.Path;
import java.util.stream.Collectors;
import org.eclipse.collections.api.factory.Sets;
import org.tinylog.Logger;

public class App {

  public static void main(String[] args) {
    DuplicateChecker duplicateChecker =
        new DuplicateChecker(
            Sets.immutable.of(
                Path.of("C:\\TEMP\\testdup\\dira"), Path.of("C:\\TEMP\\testdup\\dirb")));

    duplicateChecker.checkDuplicates();

    duplicateChecker
        .getPathSubSetOf()
        .forEachKey(
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
  }
}
