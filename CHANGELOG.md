# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Calendar Versioning](https://calver.org/) with the scheme `YYYY-0M-0D`.

## [Unreleased]

### Added

- `kodf scan` records the files below the given directories in an SQLite database; a rescan reads only changed files.
- `kodf report` lists the directories whose whole content also exists in another directory, wherever that one lies.
- Daily release: the changes of a day are merged into `main` and tagged with the date as version.

### Removed

- The previous prototype with its hard-coded scan path.

[Unreleased]: https://github.com/koppor/kodf/compare/main...experimental
