package io.github.koppor.kodf.database;

import java.nio.file.Path;
import lombok.Builder;
import lombok.Getter;

@Builder
public class FileData implements FsObjectData {

  @Getter private final Path path;

  @Getter private final long size;

  @Getter(lazy = true)
  private final Path parent = determineDirectory();

  private Path determineDirectory() {
    return path.getParent();
  }
}
