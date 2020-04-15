package io.github.koppor.kodf;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import lombok.RequiredArgsConstructor;
import me.tongfei.progressbar.ProgressBar;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.MutableSet;
import org.tinylog.Logger;

@RequiredArgsConstructor
public class FileCollector implements FileVisitor<Path> {

  private final MutableMap<Path, DirData> pathToDirData;
  private final MutableSet<FileData> allFiles;
  private final ProgressBar progressBar;

  private DirData currentDirectory;

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    Logger.debug("Visiting {}...", dir.toString());
    currentDirectory = new DirData(dir);
    pathToDirData.put(dir, currentDirectory);
    progressBar.step();
    progressBar.setExtraMessage(dir.toString());
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    if (!attrs.isRegularFile()) {
      return FileVisitResult.CONTINUE;
    }
    FileData fileData = new FileData(file, attrs.size());
    currentDirectory.files.add(fileData);
    allFiles.add(fileData);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
    Logger.error(exc, "Visit file failed for {}", file);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
    return FileVisitResult.CONTINUE;
  }
}
