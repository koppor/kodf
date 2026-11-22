package io.github.koppor.kodf;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// [impl->dsn~sqlite-index~1]
/** The scan results: one row per regular file, with its content hash once it is known. */
final class Db {

  private Db() {}

  static Connection open(Path file) throws SQLException {
    Connection db = DriverManager.getConnection("jdbc:sqlite:" + file);
    try (Statement s = db.createStatement()) {
      s.execute("PRAGMA journal_mode=WAL");
      s.execute(
          """
          CREATE TABLE IF NOT EXISTS file (
            path  TEXT PRIMARY KEY,
            dir   TEXT NOT NULL,
            size  INTEGER NOT NULL,
            mtime INTEGER NOT NULL,
            hash  TEXT
          )\
          """);
      s.execute("CREATE INDEX IF NOT EXISTS file_size ON file(size)");
    }
    return db;
  }
}
