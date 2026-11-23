package io.github.koppor.kodf;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

/**
 * Records all files below the given roots and hashes those whose size is shared with another file:
 * a file with a unique size cannot have a duplicate, so it is never read.
 */
final class Scanner {

    // [impl->dsn~ignored-directories~1]
    /** Synology metadata, recycle bins and snapshots are not user data. */
    static final Set<String> IGNORED = Set.of("@eaDir", "#recycle", "#snapshot", "$RECYCLE.BIN", ".git");

    private static final int COMMIT_EVERY = 100;

    /** Size recorded for a file or directory that could not be read. */
    static final long UNREADABLE = -1;

    private final Connection db;
    private final long minSize;

    Scanner(Connection db, long minSize) {
        this.db = db;
        // Empty files are all equal and say nothing about where data is duplicated.
        this.minSize = Math.max(1, minSize);
    }

    void scan(List<Path> roots) throws IOException, SQLException {
        db.setAutoCommit(false);
        try (Statement s = db.createStatement()) {
            s.execute("CREATE TEMP TABLE IF NOT EXISTS seen (path TEXT PRIMARY KEY)");
            s.execute("DELETE FROM seen");
        }
        for (Path root : roots) {
            collect(root.toAbsolutePath().normalize());
        }
        db.commit();
        hash();
        db.setAutoCommit(true);
    }

    // [impl->dsn~incremental-rescan~1]
    private void collect(Path root) throws IOException, SQLException {
        // [impl->dsn~vanished-root~1]
        // A scanned directory that no longer exists only loses its rows below; walking it would record it as
        // unreadable and block its parent.
        boolean vanished = Files.notExists(root);
        if (vanished) {
            System.err.println(root + " no longer exists, its results are removed");
        }
        // An unchanged size and modification time keep the known hash, so a rescan reads only
        // changed files.
        try (PreparedStatement upsert = db.prepareStatement("""
            INSERT INTO file (path, dir, size, mtime) VALUES (?, ?, ?, ?)
            ON CONFLICT (path) DO UPDATE SET
              hash = CASE WHEN size = excluded.size AND mtime = excluded.mtime
                     THEN hash END,
              size = excluded.size, mtime = excluded.mtime\
            """);
                PreparedStatement seen = db.prepareStatement("INSERT OR IGNORE INTO seen VALUES (?)")) {
            if (!vanished)
                Files.walkFileTree(root, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        Path name = dir.getFileName();
                        return name != null && IGNORED.contains(name.toString())
                                ? FileVisitResult.SKIP_SUBTREE
                                : FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (!attrs.isRegularFile() || attrs.size() < minSize) {
                            return FileVisitResult.CONTINUE;
                        }
                        record(file, attrs.size(), attrs.lastModifiedTime().toMillis());
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    // [impl->dsn~unreadable-blocks-subset~1]
                    public FileVisitResult visitFileFailed(Path file, IOException e) {
                        System.err.println("Cannot read " + file + ": " + e.getMessage());
                        // A row without hash makes the directories around it unique, as their content is not
                        // fully known.
                        record(file, UNREADABLE, 0);
                        return FileVisitResult.CONTINUE;
                    }

                    private void record(Path file, long size, long mtime) {
                        try {
                            upsert.setString(1, file.toString());
                            upsert.setString(2, file.getParent().toString());
                            upsert.setLong(3, size);
                            upsert.setLong(4, mtime);
                            upsert.executeUpdate();
                            seen.setString(1, file.toString());
                            seen.executeUpdate();
                        } catch (SQLException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                });
        }
        try (PreparedStatement gone = db.prepareStatement("""
            DELETE FROM file
            WHERE (path = ? OR substr(path, 1, length(?)) = ?)
              AND path NOT IN (SELECT path FROM seen)\
            """)) {
            String prefix = root.toString().endsWith(root.getFileSystem().getSeparator())
                    ? root.toString()
                    : root + root.getFileSystem().getSeparator();
            gone.setString(1, root.toString());
            gone.setString(2, prefix);
            gone.setString(3, prefix);
            gone.executeUpdate();
        }
    }

    // [impl->dsn~hash-size-collisions~1]
    private void hash() throws SQLException {
        try (Statement s = db.createStatement();
                ResultSet rs = s.executeQuery("""
                    SELECT path FROM file
                    WHERE hash IS NULL AND size > 0
                      AND size IN (SELECT size FROM file GROUP BY size HAVING count(*) > 1)\
                    """);
                PreparedStatement update = db.prepareStatement("UPDATE file SET hash = ? WHERE path = ?")) {
            int pending = 0;
            while (rs.next()) {
                String path = rs.getString(1);
                try {
                    update.setString(1, sha256(Path.of(path)));
                } catch (IOException e) {
                    System.err.println("Cannot read " + path + ": " + e.getMessage());
                    continue;
                }
                update.setString(2, path);
                update.executeUpdate();
                if (++pending == COMMIT_EVERY) {
                    db.commit();
                    pending = 0;
                }
            }
        }
        db.commit();
    }

    static String sha256(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[1 << 16];
            for (int n; (n = in.read(buffer)) > 0; ) {
                digest.update(buffer, 0, n);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }
}
