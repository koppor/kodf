package io.github.koppor.kodf;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = "kodf",
    mixinStandardHelpOptions = true,
    description = "Finds directories whose content exists elsewhere.",
    subcommands = {App.Scan.class, App.Report.class})
public class App {

  @Option(
      names = "--db",
      defaultValue = "kodf.db",
      scope = CommandLine.ScopeType.INHERIT,
      description = "SQLite file holding the scan results (default: ${DEFAULT-VALUE})")
  Path db;

  public static void main(String[] args) {
    System.exit(new CommandLine(new App()).execute(args));
  }

  @Command(name = "scan", description = "Records the files below the given directories.")
  static class Scan implements Callable<Integer> {
    @CommandLine.ParentCommand App app;

    @Parameters(arity = "1..*", paramLabel = "DIR")
    List<Path> roots;

    @Override
    public Integer call() throws Exception {
      try (Connection db = Db.open(app.db)) {
        new Scanner(db).scan(roots);
      }
      return 0;
    }
  }

  @Command(name = "report", description = "Lists directories whose content exists elsewhere.")
  static class Report implements Callable<Integer> {
    @CommandLine.ParentCommand App app;

    @Override
    public Integer call() throws Exception {
      try (Connection db = Db.open(app.db)) {
        Subsets.find(db)
            .forEach((dir, supersets) -> {
              System.out.println(dir);
              supersets.forEach(s -> System.out.println("  contained in " + s));
            });
      }
      return 0;
    }
  }
}
