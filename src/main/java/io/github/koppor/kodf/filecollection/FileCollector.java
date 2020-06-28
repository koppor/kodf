package io.github.koppor.kodf.filecollection;

import io.github.koppor.kodf.database.DirData;
import io.github.koppor.kodf.database.FileData;
import io.github.koppor.kodf.database.FsObjectData;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.tinylog.Logger;

/** Collects all non-empty directories. Ignores given directories */
@RequiredArgsConstructor
public class FileCollector implements FileVisitor<Path> {

  private final Set<Path> pathsToIgnore; // immutable
  private final ProgressBar progressBar;
  private final List<DirData> allDirs;

  private final MultiValuedMap<Path, FsObjectData> childrenMap = new HashSetValuedHashMap<>();

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    Logger.debug("Visiting {}...", dir.toString());
    progressBar.step();
    progressBar.setExtraMessage(dir.toString());
    if (pathsToIgnore.contains(dir) || "@eadir".equals(dir.getFileName()) || "$RECYCLE.BIN".equals(dir.getFileName())) {
      Logger.debug("Ignoring directory");
      return FileVisitResult.SKIP_SUBTREE;
    }
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    if (!attrs.isRegularFile()) {
      return FileVisitResult.CONTINUE;
    }

    FileData fileData = FileData.builder().path(file).size(attrs.size()).build();

    childrenMap.put(fileData.getParent(), fileData);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
    Logger.error(exc, "Visit file failed for {}", file);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
    Collection<FsObjectData> children = childrenMap.get(dir);
    if (CollectionUtils.isNotEmpty(children)) {
      DirData.DirDataBuilder visitedDirBuilder = DirData.builder().path(dir);
      children.forEach(visitedDirBuilder::fsObjectData);
      DirData dirData = visitedDirBuilder.build();
      childrenMap.put(dirData.getParent(), dirData);
      allDirs.add(dirData);
    } else {
      Logger.debug("Ignoring empty directory {}", dir);
    }
    return FileVisitResult.CONTINUE;
  }
}
