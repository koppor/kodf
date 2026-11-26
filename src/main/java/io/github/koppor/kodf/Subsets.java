package io.github.koppor.kodf;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
            List<Path> smallest = candidates.stream()
                    .filter(c -> candidates.stream().noneMatch(o -> o != c && o.startsWith(c)))
                    .sorted()
                    .toList();
            if (!smallest.isEmpty()) {
                result.put(dir, smallest);
            }
        });
        Set<Path> reported = Set.copyOf(result.keySet());
        result.keySet().removeIf(dir -> {
            for (Path p = dir.getParent(); p != null; p = p.getParent()) {
                if (reported.contains(p)) {
                    return true;
                }
            }
            return false;
        });
        return result;
    }

    // [impl->dsn~shared-data-report~1]
    /** Whether every file below {@code dir} shares its data on disk with a file below {@code superset}. */
    static boolean sharesData(Connection db, Path dir, Path superset) throws SQLException {
        // Path ranges instead of prefix comparisons, so that SQLite uses the primary key index.
        try (PreparedStatement s = db.prepareStatement("""
            SELECT NOT EXISTS (
              SELECT 1 FROM file a
              WHERE a.path >= ?1 AND a.path < ?2
                AND (a.extents IS NULL OR NOT EXISTS (
                  SELECT 1 FROM file b WHERE b.extents = a.extents AND b.path >= ?3 AND b.path < ?4)))\
            """)) {
            String inDir = below(dir);
            String inSuperset = below(superset);
            s.setString(1, inDir);
            s.setString(2, after(inDir));
            s.setString(3, inSuperset);
            s.setString(4, after(inSuperset));
            try (ResultSet rs = s.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    /** The smallest string greater than all strings starting with {@code prefix}. */
    private static String after(String prefix) {
        return prefix.substring(0, prefix.length() - 1) + (char) (prefix.charAt(prefix.length() - 1) + 1);
    }

    private static String below(Path dir) {
        String separator = dir.getFileSystem().getSeparator();
        return dir.toString().endsWith(separator) ? dir.toString() : dir + separator;
    }
}
