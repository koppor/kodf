package io.github.koppor.kodf.duplication;

import io.github.koppor.kodf.database.DirData;
import io.github.koppor.kodf.database.FileData;
import io.github.koppor.kodf.filecollection.FileCollector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import org.apache.commons.collections4.MultiSet;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.io.FileUtils;
import org.tinylog.Logger;

@RequiredArgsConstructor
public class DuplicateChecker {

  @NonNull private final DuplicateCheckerConfig config;

  @Getter
  private final MultiValuedMap<DirData, DirData> knownSuperSets = new HashSetValuedHashMap<>();

  @Getter
  private final DuplicationTracker<FileData> fileDuplicationTracker = new DuplicationTracker<>();

  public void run() {

    List<DirData> allDirs = scanFiles(); // TODO consider inlining

    Map<Path, DirData> pathToDirData = new HashedMap<>();

    MultiValuedMap<Long, DirData> dirsBySize = new HashSetValuedHashMap<>();
    MultiValuedMap<Long, FileData> filesBySize = new HashSetValuedHashMap<>();
    prepareIndexes(allDirs, dirsBySize, filesBySize);
    findDuplicateFiles(filesBySize);
    analyzeDirectories(dirsBySize);
    printFileDuplicates(); // TODO refactor into appropriate class, output does not belong here
    printDirectorySubsets(); // TODO refactor into appropriate class, output does not belong here
  }

  private void printDirectorySubsets() {
    System.out.println("Directory subsets:");
    for (DirData dirData : knownSuperSets.keySet()) {
      knownSuperSets
          .get(dirData)
          .forEach(
              it -> {
                System.out.println(dirData.getPath() + " -> " + it.getPath());
              });
    }
  }

  private void printFileDuplicates() {
    System.out.println("File duplicates:");
    for (Collection<FileData> duplicationSet : fileDuplicationTracker.getDuplicationSets()) {
      System.out.println("-------------");
      duplicationSet.forEach(it -> System.out.println(it.getPath()));
    }
  }

  private void analyzeDirectories(MultiValuedMap<Long, DirData> dirsBySize) {
    List<Long> sortedSizes =
        dirsBySize.keySet().stream().sorted().collect(Collectors.toUnmodifiableList());
    List<DirData> smallerDirs = new ArrayList<>(dirsBySize.size());
    for (Long dirSize : ProgressBar.wrap(sortedSizes, "Analyze Dirs")) {
      Collection<DirData> currentDirCollection = dirsBySize.get(dirSize);
      for (DirData superSetCandidate : currentDirCollection) {
        for (DirData subSetCandidate : smallerDirs) {
          if (isSuperSetMatch(superSetCandidate, subSetCandidate)) {
            knownSuperSets.put(subSetCandidate, superSetCandidate);
          }
        }
        smallerDirs.add(superSetCandidate);
      }
    }
  }

  private boolean isSuperSetMatch(DirData superSetCandidate, DirData subSetCandidate) {
    for (FileData file : subSetCandidate.getFiles()) {
      Collection<FileData> duplicates = fileDuplicationTracker.getDuplicates(file);
      if (Collections.disjoint(
          duplicates, superSetCandidate.getFilesBySize().get(file.getSize()))) {
        return false;
      }
    }
    Collection<DirData> superSetSubDirs = superSetCandidate.getDirsBySize().values();
    for (DirData subSetCandidateCurrentDir : subSetCandidate.getDirsBySize().values()) {
      if (Collections.disjoint(knownSuperSets.get(subSetCandidateCurrentDir), superSetSubDirs)) {
        return false;
      }
    }
    return true;
  }

  private void findDuplicateFiles(MultiValuedMap<Long, FileData> filesBySize) {
    MultiSet<Long> fileSizes = filesBySize.keys();
    for (Long fileSize : ProgressBar.wrap(fileSizes, "Compare Files")) {
      Collection<FileData> fileDataCollection = filesBySize.get(fileSize);
      Collection<FileData> compFiles = new ArrayList<>(fileDataCollection.size());
      for (FileData currentFileToCheck : fileDataCollection) {
        for (FileData compFile : compFiles) {
          try {
            if (FileUtils.contentEquals(
                currentFileToCheck.getPath().toFile(), compFile.getPath().toFile())) {
              fileDuplicationTracker.addDuplicate(currentFileToCheck, compFile);
            }
          } catch (IOException e) {
            Logger.warn(
                "Error comparing {} and {}", currentFileToCheck.getPath(), compFile.getPath());
          }
        }
        compFiles.add(currentFileToCheck);
      }
    }
  }

  private void prepareIndexes(
      List<DirData> allDirs,
      MultiValuedMap<Long, DirData> dirsBySize,
      MultiValuedMap<Long, FileData> filesBySize) {
    for (DirData currentDir : ProgressBar.wrap(allDirs, "Index Directories")) {
      filesBySize.putAll(currentDir.getFilesBySize());
      dirsBySize.putAll(currentDir.getDirsBySize());
    }
  }

  private List<DirData> scanFiles() {
    // collect all files
    // provided 100 instead of -1, because of Exception in thread
    // "me.tongfei.progressbar.ProgressBar" java.lang.ArithmeticException: / by zero at
    // me.tongfei.progressbar.DefaultProgressBarRenderer.render(DefaultProgressBarRenderer.java:96)

    List<DirData> allDirs = new ArrayList<>();

    try (ProgressBar progressBar =
        new ProgressBarBuilder().setTaskName("Collect all files").setInitialMax(1L).build(); ) {
      config
          .getPathsToScan()
          .forEach(
              root -> {
                try {
                  Files.walkFileTree(
                      root, new FileCollector(config.getPathsToIgnore(), progressBar, allDirs));
                } catch (IOException e) {
                  Logger.error(e, "Could not visit {}", root);
                }
              });
    }
    return allDirs;
  }
}
