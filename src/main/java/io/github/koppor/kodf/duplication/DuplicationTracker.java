package io.github.koppor.kodf.duplication;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

@NoArgsConstructor
public class DuplicationTracker<T> {

  private long nextIndex = 0;

  private final Map<T, Long> fileDuplicationIndex = new HashedMap<>();
  private final MultiValuedMap<Long, T> fileDuplicates = new HashSetValuedHashMap<>();

  public void addDuplicate(T a, T b) {
    if (hasDuplicates(a) && hasDuplicates(b)) {
      mergeDuplicates(a, b);
    } else if (hasDuplicates(a)) {
      Long index = fileDuplicationIndex.get(a);
      fileDuplicationIndex.put(b, index);
      fileDuplicates.put(index, b);
    } else if (hasDuplicates(b)) {
      addDuplicate(b, a);
    } else {
      long index = nextIndex++;
      fileDuplicationIndex.put(b, index);
      fileDuplicates.put(index, a);
      fileDuplicates.put(index, b);
    }
  }

  private void mergeDuplicates(T a, T b) {
    if (!Objects.equals(fileDuplicationIndex.get(a), fileDuplicationIndex.get(b))) {
      Collection<T> tsInA = fileDuplicates.get(fileDuplicationIndex.get(a));
      fileDuplicates.putAll(fileDuplicationIndex.get(b), tsInA);
      tsInA.forEach(it -> fileDuplicationIndex.put(it, fileDuplicationIndex.get(b)));
    }
  }

  public boolean hasDuplicates(T a) {
    return fileDuplicationIndex.containsKey(a);
  }

  public Collection<T> getDuplicates(T a) {
    return hasDuplicates(a)
        ? fileDuplicates.get(fileDuplicationIndex.get(a))
        : Collections.EMPTY_LIST;
  }

  public Collection<Collection<T>> getDuplicationSets() {
    return fileDuplicates.asMap().values();
  }
}
