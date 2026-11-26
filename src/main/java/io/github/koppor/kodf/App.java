// [impl->adr~jbang~1]
// The build reads JAVA and DEPS from here, so they are declared only once.
//JAVA 27
//DEPS info.picocli:picocli:4.7.7
//DEPS org.xerial:sqlite-jdbc:3.54.1.0
//DEPS org.webjars.npm:d3:7.9.2
//SOURCES Db.java Duplicates.java Extents.java Scanner.java Serve.java Subsets.java
//FILES io/github/koppor/kodf/viewer.html=../../../../../resources/io/github/koppor/kodf/viewer.html
//RUNTIME_OPTIONS --enable-native-access=ALL-UNNAMED

package io.github.koppor.kodf;

import java.net.InetSocketAddress;
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
        subcommands = {App.Scan.class, App.Report.class, App.Serve.class})
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
        @CommandLine.ParentCommand
        App app;

        @Parameters(arity = "1..*", paramLabel = "DIR")
        List<Path> roots;

        // [impl->dsn~min-size~1]
        @Option(
                names = "--min-size",
                defaultValue = "1",
                description = "Smallest file size in bytes to record (default: ${DEFAULT-VALUE}). Smaller files are"
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
        @CommandLine.ParentCommand
        App app;

        // [impl->dsn~fdupes-groups~1]
        @Option(
                names = "--fdupes",
                description = "Lists the groups of identical files instead, one path per line and a blank line after"
                        + " each group, as fdupes does: `kodf report --fdupes | duperemove --fdupes -d` lets them"
                        + " share their data on btrfs or XFS.")
        boolean fdupes;

        @Override
        public Integer call() throws Exception {
            try (Connection db = Db.open(app.db)) {
                if (fdupes) {
                    for (var group : Duplicates.find(db)) {
                        group.forEach(System.out::println);
                        System.out.println();
                    }
                    return 0;
                }
                for (var entry : Subsets.find(db).entrySet()) {
                    System.out.println(entry.getKey());
                    for (var superset : entry.getValue()) {
                        System.out.println("  contained in " + superset
                                + (Subsets.sharesData(db, entry.getKey(), superset)
                                        ? " (already shares its data, deleting frees no space)"
                                        : ""));
                    }
                }
            }
            return 0;
        }
    }

    @Command(name = "serve", description = "Shows the scan results as a web page.")
    static class Serve implements Callable<Integer> {
        @CommandLine.ParentCommand
        App app;

        @Option(names = "--port", defaultValue = "8080", description = "Port to listen on (default: ${DEFAULT-VALUE})")
        int port;

        @Option(
                names = "--address",
                defaultValue = "127.0.0.1",
                description = "Address to listen on (default: ${DEFAULT-VALUE}, only this machine). 0.0.0.0 lets every"
                        + " machine in the network see the directory names.")
        String address;

        @Override
        public Integer call() throws Exception {
            io.github.koppor.kodf.Serve.start(app.db, new InetSocketAddress(address, port));
            System.out.println("Serving http://" + address + ":" + port + "/, stop with Ctrl+C");
            Thread.currentThread().join();
            return 0;
        }
    }
}
