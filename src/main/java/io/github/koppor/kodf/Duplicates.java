package io.github.koppor.kodf;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class Duplicates {

    private Duplicates() {}

    // [impl->dsn~fdupes-groups~1]
    /** Groups of files with the same content, leaving out groups whose files all share their data already. */
    static List<List<Path>> find(Connection db) throws SQLException {
        List<List<Path>> groups = new ArrayList<>();
        try (Statement s = db.createStatement();
                ResultSet rs = s.executeQuery("""
                    SELECT hash, path FROM file
                    WHERE hash IN (
                      SELECT hash FROM file WHERE hash IS NOT NULL GROUP BY hash
                      HAVING count(*) > 1 AND NOT (count(extents) = count(*) AND count(DISTINCT extents) = 1))
                    ORDER BY hash, path\
                    """)) {
            String previous = null;
            while (rs.next()) {
                if (!Objects.equals(previous, rs.getString(1))) {
                    groups.add(new ArrayList<>());
                    previous = rs.getString(1);
                }
                groups.getLast().add(Path.of(rs.getString(2)));
            }
        }
        return groups;
    }
}
