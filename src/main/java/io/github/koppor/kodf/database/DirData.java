package io.github.koppor.kodf.database;

import java.nio.file.Path;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Accessors;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.multimap.list.FastListMultimap;

@Value
@Accessors(fluent = true)
public class DirData {

  public final Path dir;

  public final MutableSet<FileData> files = Sets.mutable.empty();

  @Getter(lazy = true)
  private final MutableMultimap<Long, FileData> sizeToFileData = determineSizeToFileData();

  @Getter(lazy = true)
  private final MutableMultimap<Long, FileData> hashCodeToFileData = determineHashCodeToFileData();

  private MutableMultimap<Long, FileData> determineHashCodeToFileData() {
    MutableMultimap<Long, FileData> result = FastListMultimap.newMultimap();
    files.forEach(file -> result.put(file.hashValue(), file));
    return result;
  }

  private MutableMultimap<Long, FileData> determineSizeToFileData() {
    MutableMultimap<Long, FileData> result = FastListMultimap.newMultimap();
    files.forEach(file -> result.put(file.size(), file));
    return result;
  }
}
