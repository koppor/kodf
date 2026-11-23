// spotless:off
// [impl->adr~jbang~1]
// The build reads JAVA and DEPS from here, so they are declared only once.
//JAVA 27
//DEPS info.picocli:picocli:4.7.7
//DEPS org.xerial:sqlite-jdbc:3.54.1.0
//SOURCES Db.java Scanner.java Subsets.java
//RUNTIME_OPTIONS --enable-native-access=ALL-UNNAMED
// spotless:on

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

    // [impl->dsn~min-size~1]
    @Option(
        names = "--min-size",
        defaultValue = "1",
        description =
            "Smallest file size in bytes to record (default: ${DEFAULT-VALUE}). Smaller files are"
                + " left out entirely: a directory reported as a subset may still hold such files.")
    long minSize;

    @Override
    public Integer call() throws Exception {
      try (Connection db = Db.open(app.db)) {
        new Scanner(db, minSize).scan(roots);
      }
      return 0;
    }
  }

  // [impl->dsn~report~1]
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
