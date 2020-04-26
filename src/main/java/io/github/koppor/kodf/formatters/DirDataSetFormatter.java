package io.github.koppor.kodf.formatters;

import io.github.koppor.kodf.database.DirData;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

public final class DirDataSetFormatter {

  public static String format(Set<DirData> set) {
    return set.stream()
        .map(DirData::dir)
        .map(Path::toString)
        .collect(Collectors.joining(", ", "[", "]"));
  }
}
