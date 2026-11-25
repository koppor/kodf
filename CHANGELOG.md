# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Calendar Versioning](https://calver.org/) with the scheme `YYYY-0M-0D`.

## [2026-11-24] - 2026-11-24

### Added

- Each daily release is published as a GitHub release with its section of the changelog.
- kodf runs without installing JBang through gg.cmd: `sh ./gg.cmd jbang kodf@koppor/kodf`.
- On btrfs, the report tells when a listed directory already shares its data with the containing one, so that deleting it frees no space.
- On btrfs, files sharing their data with a file already read are not read again.
- `kodf report --fdupes` lists the groups of identical files for `duperemove --fdupes -d`, which lets them share their data on btrfs or XFS.

### Fixed

- A directory holding something unreadable is no longer reported as contained elsewhere.
- Rescanning a directory that no longer exists removes its results, instead of keeping its parent directory from being reported.

## [2026-11-22] - 2026-11-22

### Added

- `kodf scan` records the files below the given directories in an SQLite database; a rescan reads only changed files.
- `kodf report` lists the directories whose whole content also exists in another directory, wherever that one lies.
- `kodf scan --min-size` leaves out small files.
- kodf runs with JBang: `jbang kodf@koppor/kodf`.
- Daily release: the changes of a day are merged into `main` and tagged with the date as version.

### Removed

- The previous prototype with its hard-coded scan path.

[2026-11-24]: https://github.com/koppor/kodf/compare/v2026-11-22...v2026-11-24
[2026-11-22]: https://github.com/koppor/kodf/releases/tag/v2026-11-22
