package io.github.koppor.kodf;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Finds directories whose whole content also exists in another directory, wherever in the scanned
 * trees that one lies. Such a directory can be deleted without losing data.
 */
final class Subsets {

  private Subsets() {}

  /**
   * Maps each such directory to the smallest directories containing all of its content. A directory
   * is left out when one of its ancestors is reported already.
   */
  // [impl->dsn~subset-anywhere~1]
  // [impl->dsn~outermost-subset-smallest-superset~1]
  static Map<Path, List<Path>> find(Connection db) throws SQLException {
    // ponytail: every file's hash is added to all its ancestors, memory is files × depth; switch
    // to a bottom-up merge in SQL if a NAS scan does not fit in the heap.
    Map<Path, Set<String>> contents = new HashMap<>();
    Map<String, Set<Path>> holders = new HashMap<>();
    // A file nobody else has the size of was not hashed: its directories cannot be subsets.
    Set<Path> unique = new HashSet<>();
    try (Statement s = db.createStatement();
        ResultSet rs = s.executeQuery("SELECT dir, hash FROM file")) {
      while (rs.next()) {
        String hash = rs.getString(2);
        for (Path p = Path.of(rs.getString(1)); p != null; p = p.getParent()) {
          if (hash == null) {
            unique.add(p);
          } else {
            contents.computeIfAbsent(p, _ -> new HashSet<>()).add(hash);
            holders.computeIfAbsent(hash, _ -> new HashSet<>()).add(p);
          }
        }
      }
    }

    Map<Path, List<Path>> result = new TreeMap<>();
    contents.forEach((dir, hashes) -> {
      if (unique.contains(dir)) {
        return;
      }
      List<String> rarestFirst = new ArrayList<>(hashes);
      rarestFirst.sort(Comparator.comparingInt(h -> holders.get(h).size()));
      Set<Path> candidates = new HashSet<>(holders.get(rarestFirst.getFirst()));
      // Ancestors and descendants trivially share the content; deleting one would delete both.
      candidates.removeIf(c -> c.startsWith(dir) || dir.startsWith(c));
      for (String h : rarestFirst) {
        if (candidates.isEmpty()) {
          return;
        }
        candidates.retainAll(holders.get(h));
      }
      List<Path> smallest =
          candidates.stream()
              .filter(c -> candidates.stream().noneMatch(o -> o != c && o.startsWith(c)))
              .sorted()
              .toList();
      if (!smallest.isEmpty()) {
        result.put(dir, smallest);
      }
    });
    Set<Path> reported = Set.copyOf(result.keySet());
    result
        .keySet()
        .removeIf(dir -> {
          for (Path p = dir.getParent(); p != null; p = p.getParent()) {
            if (reported.contains(p)) {
              return true;
            }
          }
          return false;
        });
    return result;
  }
}
