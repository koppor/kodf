package io.github.koppor.kodf.database;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.nio.file.FileSystem;
import org.junit.jupiter.api.Test;

class DirDataTest {

  private final FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

  @Test
  void sizeToFileData() {}

  @Test
  void hashCodeToFileData() {}
}
