package io.github.koppor.kodf;

import io.github.koppor.kodf.formatters.DirDataSetFormatter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import me.tongfei.progressbar.ProgressBar;
import org.eclipse.collections.api.collection.MutableCollection;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.multimap.ImmutableMultimap;
import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.api.multimap.set.MutableSetMultimap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.multimap.list.FastListMultimap;
import org.eclipse.collections.impl.multimap.set.SynchronizedPutUnifiedSetMultimap;
import org.tinylog.Logger;

@Builder
@RequiredArgsConstructor
public class DuplicateChecker {

  private final ImmutableSet<Path> pathsToScan;

  @Builder.Default private final ImmutableSet<Path> pathsToKeep = Sets.immutable.empty();

  @Builder.Default private final ImmutableSet<Path> pathsToIgnore = Sets.immutable.empty();

  private MutableMultimap<Path, Path> $pathSubSetOf;

  public ImmutableMultimap<Path, Path> getPathSubSetOf() {
    return $pathSubSetOf.toImmutable();
  }

  public void checkDuplicates() {
    $pathSubSetOf = FastListMultimap.newMultimap();

    MutableMap<Path, DirData> pathToDirData = Maps.mutable.empty();
    MutableSet<FileData> allFiles = Sets.mutable.empty();

    // collect all files
    // provided 100 instead of -1, because of Exception in thread
    // "me.tongfei.progressbar.ProgressBar" java.lang.ArithmeticException: / by zero at
    // me.tongfei.progressbar.DefaultProgressBarRenderer.render(DefaultProgressBarRenderer.java:96)
    try (ProgressBar progressBar = new ProgressBar("Collect all files", 100)) {
      pathsToScan.forEach(
          root -> {
            try {
              Files.walkFileTree(
                  root, new FileCollector(pathToDirData, allFiles, pathsToIgnore, progressBar));
            } catch (IOException e) {
              Logger.error(e, "Could not visit {}", root);
            }
          });
    }

    // fill sizeToDirData
    MutableSetMultimap<Long, DirData> sizeToDirData =
        SynchronizedPutUnifiedSetMultimap.newMultimap();
    allFiles
        .parallelStream()
        .forEach(fileData -> sizeToDirData.put(fileData.size(), pathToDirData.get(fileData.dir())));

    // determine map from size to set of DirData (which are candidates from the view of the size)
    MutableSetMultimap<Long, DirData> sizeCandiates =
        sizeToDirData.rejectKeysMultiValues(
            (size, dirDataIterable) -> {
              Iterator<DirData> iterator = dirDataIterable.iterator();
              iterator.next();
              // reject if only one value
              return !iterator.hasNext();
            });

    // TODO: a bit conservative: a directory is a "subset" if the amount of files is also less or
    // equal

    try (ProgressBar progressBar = new ProgressBar("Compare directories", pathToDirData.size())) {
      // check each path if it is fully contained
      pathToDirData.forEachKeyValue(
          (path, dirData) -> {
            Logger.debug("Checking {}...", path);
            Iterator<FileData> fileDataIterator = dirData.files.iterator();
            assert fileDataIterator.hasNext();
            FileData firstFileData = fileDataIterator.next();
            MutableSet<DirData> allDirsWhereAllFileSizesAppear =
                sizeCandiates.get(firstFileData.size()).toSet();
            // directory itself is not a candidate for other (!) directory
            allDirsWhereAllFileSizesAppear.remove(pathToDirData.get(path));
            // search through all files
            while (fileDataIterator.hasNext() && !allDirsWhereAllFileSizesAppear.isEmpty()) {
              FileData currentFileData = fileDataIterator.next();
              MutableSet<DirData> dirDataOfCurrentFile = sizeCandiates.get(currentFileData.size());
              allDirsWhereAllFileSizesAppear =
                  allDirsWhereAllFileSizesAppear.intersect(dirDataOfCurrentFile);
            }
            if (allDirsWhereAllFileSizesAppear.isEmpty()) {
              // no common directories found
              return;
            }

            Logger.debug(
                "Directories where {} is contained (size match): {}",
                path,
                DirDataSetFormatter.format(allDirsWhereAllFileSizesAppear));

            // checksum-based matching

            allDirsWhereAllFileSizesAppear.reject(
                otherDirData -> {
                  Iterator<FileData> thisFileDataIterator = dirData.files.iterator();
                  boolean hashMatch;
                  do {
                    FileData thisFileData = thisFileDataIterator.next();
                    MutableCollection<FileData> otherFileDataHavingMatchingHashes =
                        otherDirData.hashCodeToFileData().get(thisFileData.hashValue());
                    hashMatch =
                        otherFileDataHavingMatchingHashes.anySatisfy(
                            eqFileData -> eqFileData.size().equals(thisFileData.size()));
                  } while (hashMatch && thisFileDataIterator.hasNext());
                  return !hashMatch;
                });

            Logger.debug(
                "Directories where {} is contained: {}",
                path,
                DirDataSetFormatter.format(allDirsWhereAllFileSizesAppear));

            // collect result
            $pathSubSetOf.putAll(path, allDirsWhereAllFileSizesAppear.collect(x -> x.dir()));

            progressBar.step();
          });
    }
  }
}
