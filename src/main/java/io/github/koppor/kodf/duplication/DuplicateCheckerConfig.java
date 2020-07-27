package io.github.koppor.kodf.duplication;

import java.nio.file.Path;
import java.util.Set;
import lombok.*;

@Builder
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DuplicateCheckerConfig {

  @NonNull
  @Singular("pathToScan")
  Set<Path> pathsToScan;

  @NonNull
  @Singular("pathsToKeep")
  Set<Path> pathsToKeep;

  @NonNull
  @Singular("pathsToIgnore")
  Set<Path> pathsToIgnore;
}
