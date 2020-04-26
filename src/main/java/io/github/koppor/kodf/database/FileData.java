package io.github.koppor.kodf.database;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import java.io.IOException;
import java.nio.file.Path;
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
  private final HashCode hashValue = calculateHashCode();

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

  private Path determineDirectory() {
    return file.getParent();
  }
}
