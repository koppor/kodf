package io.github.koppor.kodf.database;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

@Builder
public class DirData implements FsObjectData {

  @Singular private final Set<FileData> files;

  @Singular private final Set<DirData> dirs;

  @Getter private final Path path;

  @Getter(lazy = true)
  private final long size = determineSize();

  @Getter(lazy = true)
  private final MultiValuedMap<Long, FileData> filesBySize = determineSizeToFileData();

  @Getter(lazy = true)
  private final MultiValuedMap<Long, DirData> dirsBySize = determineSizeToDirData();

  @Getter(lazy = true)
  private final Path parent = determineDirectory();

  public Set<FileData> getFiles() {
    return Collections.unmodifiableSet(files);
  }

  private MultiValuedMap<Long, FileData> determineSizeToFileData() {
    MultiValuedMap<Long, FileData> result = new HashSetValuedHashMap<>();
    files.forEach(file -> result.put(file.getSize(), file));
    return result;
  }

  private MultiValuedMap<Long, DirData> determineSizeToDirData() {
    MultiValuedMap<Long, DirData> result = new HashSetValuedHashMap<>();
    dirs.forEach(dir -> result.put(dir.getSize(), dir));
    return result;
  }

  private long determineSize() {
    long fileSizeSum =
        files.stream()
            .map(FileData::getSize)
            .filter(size -> size > 0)
            .mapToLong(Long::longValue)
            .sum();
    long dirSizeSum =
        dirs.stream()
            .map(DirData::getSize)
            .filter(size -> size > 0)
            .mapToLong(Long::longValue)
            .sum();
    return fileSizeSum + dirSizeSum;
  }

  private Path determineDirectory() {
    return path.getParent();
  }

  public static class DirDataBuilder {

    public DirDataBuilder fsObjectData(FsObjectData data) {
      if (data instanceof DirData) {
        return dir((DirData) data);
      } else if (data instanceof FileData) {
        return file((FileData) data);
      } else {
        return this;
      }
    }
  }
}
