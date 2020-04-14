package io.github.koppor.kodf;

import java.io.IOException;
import java.nio.file.Path;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.tinylog.Logger;

@Builder
@Accessors(fluent = true)
public class FileData {

  @Getter private final Path file;
  @Getter(lazy = true) private final HashCode hashValue = calculateHashCode();
  @Getter(lazy = true) private final Long size = determineFileSize();

  private Long determineFileSize() {
    try {
      return java.nio.file.Files.size(file);
    } catch (IOException e) {
      Logger.error(e, "Could not determine file size for {}", file);
      return -1L;
    }
  }

  private HashCode calculateHashCode() {
    HashCode result;
    try {
      result = Files.asByteSource(file.toFile()).hash(Hashing.crc32());
    } catch (IOException e) {
      Logger.error(e, "Could not hash {}", file);
      result = HashCode.fromInt(-1);
    }
    return result;
  }
}
