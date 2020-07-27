package io.github.koppor.kodf.database;

import java.nio.file.Path;

public interface FsObjectData {
  Path getPath();

  long getSize();

  Path getParent();
}
