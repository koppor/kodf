# koppor's Duplicate Finder

Finds duplicate files in different directories.

- Inspired by [FastDuplicateFinder](https://github.com/carlbeech/fast-duplicate-finder)
- (Currently) more code-oriented
- Less UI than [dupeGuru](https://dupeguru.voltaicideas.net/)

## Distinguishing features

- **Directory subset identification**.
  Sometimes, a file-based backup is made to different places on the NAS.
  As user, I want to know if a directory can be completely erased.
- **Arbitrary duplicate identification**.
  Finds duplicate directories in arbitrary sub folders.
  For instance, if one copy of the directory is contained in both `H:\backups\2019-01-05\data-from-white-hdd` and in `H:\0-to-sort\whiteone`, it is found
  With other tools, one has to start the comparison from `H:\backups\2019-01-05` and `H:\0-to-sort`, but who knows that `2019-01-05` is the right top level directory to start from.

### Other tools

- [fdupes](https://github.com/adrianlopezroche/fdupes) - very prominent tool maintained since 1999.

## Usage

Run it on the NAS itself, so it reads the disks directly instead of over the network.
The only thing to install is [JBang](https://www.jbang.dev/download/); it downloads Java and kodf (from `main`, the latest daily release) by itself:

```terminal
jbang kodf@koppor/kodf scan /volume1/backups /volume1/to-sort
jbang kodf@koppor/kodf report
```

Example output:

```text
/volume1/backups/2019-01-05
  contained in /volume1/to-sort/whiteone
```

### What it does

- `scan DIR...` records every file below the given directories in `kodf.db`, an SQLite database (choose another file with `--db`).
  - Only files that share their size with another file are read and hashed (SHA-256); a file of unique size cannot have a duplicate.
  - A rescan reads only files whose size or modification time changed, and forgets files that are gone.
  - Empty files are not recorded.
  - `--min-size BYTES` leaves out smaller files, for a faster scan of photos and videos.
    A directory may then be reported although it holds smaller files that exist nowhere else.
  - Not scanned: `@eaDir`, `#recycle`, `#snapshot` (Synology), `$RECYCLE.BIN`, `.git`.
- `report` lists each directory whose whole content also exists in another directory, wherever that one lies, and names that directory.
  - A directory is not listed when a directory around it is listed already.
  - Only the innermost containing directories are named.
  - Two directories with the same content are both listed, each with the other.

Nothing is deleted or changed; `report` only lists.

### Planned

- A web page served by kodf that shows the directories as nested circles, and shared content as overlapping circles.
- On btrfs: recognizing files that already share their data on disk, and letting duplicates share their data instead of deleting them.

The planned scope is tracked in [docs/requirements](docs/requirements/README.md), the decisions in [docs/decisions](docs/decisions/README.md).

## Development

- Requires nothing but a JDK to start Gradle; Gradle downloads Java 27 itself.
- `./gradlew build` compiles, checks the formatting and runs the tests.
- `./gradlew spotlessApply` formats the code ([Google Java Style](https://google.github.io/styleguide/javaguide.html)).
- `jbang kodf ...` runs the checkout, `jbang trace` traces requirements and decisions with [OpenFastTrace](https://github.com/itsallcode/openfasttrace).
- The Java version and the dependencies are declared in the JBang header of `App.java`; the Gradle build reads them from there.
