package io.github.koppor.kodf.database;

import com.google.common.hash.HashCode;
import java.nio.file.Path;

import io.github.koppor.kodf.database.FileData;
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
  private final MutableMultimap<HashCode, FileData> hashCodeToFileData =
      determineHashCodeToFileData();

  private MutableMultimap<HashCode, FileData> determineHashCodeToFileData() {
    MutableMultimap<HashCode, FileData> result = FastListMultimap.newMultimap();
    files.forEach(file -> result.put(file.hashValue(), file));
    return result;
  }

  private MutableMultimap<Long, FileData> determineSizeToFileData() {
    MutableMultimap<Long, FileData> result = FastListMultimap.newMultimap();
    files.forEach(file -> result.put(file.size(), file));
    return result;
  }
}
