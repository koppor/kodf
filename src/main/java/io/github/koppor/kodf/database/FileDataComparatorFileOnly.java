package io.github.koppor.kodf.database;

import java.util.Comparator;

public class FileDataComparatorFileOnly implements Comparator<FileData> {

  @Override
  public int compare(FileData o1, FileData o2) {
    if (o1 == o2) {
      return 0;
    }
    return o1.file().compareTo(o2.file());
  }
}
