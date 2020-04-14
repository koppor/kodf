package io.github.koppor.kodf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.stream.Stream;

import com.google.common.hash.HashCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.koppor.kodf.typeadapters.FileTimeTypeAdapter;
import io.github.koppor.kodf.typeadapters.PathTypeAdapter;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.collector.Collectors2;
import org.tinylog.Logger;

public class App {

  public static final HashCode NULL_HASH_CODE = HashCode.fromInt(0);
  private static final FileTime NULL_FILE_TIME = FileTime.from(Instant.EPOCH);
  private static final Long NULL_FILE_SIZE = -1L;

  public static void main(String[] args) {
    FileData test = new FileData(Path.of("test"));
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Path.class, new PathTypeAdapter())
      .registerTypeAdapter(FileTime.class, new FileTimeTypeAdapter())
      .setPrettyPrinting()
      .create();
    String json = gson.toJson(test);
    Logger.debug(json);

    ImmutableSet<Path> pathsToScan = Sets.immutable.of(Path.of("C:\\TEMP\\testdup\\dira"));

    ImmutableSet<FileData> fileData = pathsToScan.flatCollect(root -> {
      try (Stream<Path> walk = Files.walk(root)) {
        return walk.filter(Files::isRegularFile)
                   .parallel()
                   .map(FileData::new)
                   .collect(Collectors2.toSet());
      } catch (IOException e) {
        Logger.error(e);
        return Sets.immutable.empty();
      }
    });
  }
}
