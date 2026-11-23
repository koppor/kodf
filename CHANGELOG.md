# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Calendar Versioning](https://calver.org/) with the scheme `YYYY-0M-0D`.

## [Unreleased]

### Added

- kodf runs without installing JBang through gg.cmd: `sh ./gg.cmd jbang kodf@koppor/kodf`.

## [2026-11-22] - 2026-11-22

### Added

- `kodf scan` records the files below the given directories in an SQLite database; a rescan reads only changed files.
- `kodf report` lists the directories whose whole content also exists in another directory, wherever that one lies.
- `kodf scan --min-size` leaves out small files.
- kodf runs with JBang: `jbang kodf@koppor/kodf`.
- Daily release: the changes of a day are merged into `main` and tagged with the date as version.

### Removed

- The previous prototype with its hard-coded scan path.

[Unreleased]: https://github.com/koppor/kodf/compare/v2026-11-22...HEAD
[2026-11-22]: https://github.com/koppor/kodf/releases/tag/v2026-11-22
