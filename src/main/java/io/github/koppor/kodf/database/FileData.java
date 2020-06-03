package io.github.koppor.kodf.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.tinylog.Logger;

@Builder
@RequiredArgsConstructor(staticName = "of")
@Value
@Accessors(fluent = true)
public class FileData {

  @Getter private final Path file;

  @Getter(lazy = true)
  private final Path dir = determineDirectory();

  @Getter private final Long size;

  @Getter(lazy = true)
  private final long hashValue = calculateHashCode();

  private long calculateHashCode() {
    long result;
    final int SIZE = 16 * 1024;
    byte[] bytes = new byte[SIZE];
    CRC32 crc = new CRC32();

    try (InputStream inputStream = Files.newInputStream(file)) {
      int readBytesCount = inputStream.read(bytes);
      while (readBytesCount > 0) {
        crc.update(bytes, 0, readBytesCount);
        readBytesCount = inputStream.read(bytes);
      }
      result = crc.getValue();
    } catch (IOException e) {
      Logger.error(e, "Could not hash {}", file);
      result = -1L;
    }
    return result;
  }

  private Path determineDirectory() {
    return file.getParent();
  }
}
