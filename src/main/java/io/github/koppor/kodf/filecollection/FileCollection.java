package io.github.koppor.kodf.filecollection;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "collect", description = "Collects information about the files")
public class FileCollection {

  @Option(names = "--ignore", description = "Directories to ignore")
  List<String> directoriesToIgnore = Lists.mutable.empty();
}
