package io.github.koppor.kodf;

import io.github.koppor.kodf.duplication.DuplicateChecker;
import io.github.koppor.kodf.duplication.DuplicateCheckerConfig;
import java.nio.file.Path;
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

    DuplicateCheckerConfig config =
        DuplicateCheckerConfig.builder()
            .pathToScan(
                Path.of(
                    "C:\\dev\\IdeaProjects\\kodf\\src\\test\\testdata")) // TODO read from config
            .build();

    DuplicateChecker duplicateChecker = new DuplicateChecker(config);
    duplicateChecker.run();
  }
}
