package io.github.koppor.kodf.filecollection;

import java.util.ArrayList;
import java.util.List;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "collect", description = "Collects information about the files")
public class FileCollection {

  @Option(names = "--ignore", description = "Directories to ignore")
  List<String> directoriesToIgnore = new ArrayList<>();
}
