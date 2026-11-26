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
- [duperemove](https://github.com/markfasheh/duperemove) - lets duplicate files and blocks on btrfs and XFS share their data; does not tell which directories can be deleted.

## Usage

Run it on the NAS itself, so it reads the disks directly instead of over the network.
The only thing to install is [JBang](https://www.jbang.dev/download/); it downloads Java and kodf (from `main`, the latest daily release) by itself:

```terminal
jbang kodf@koppor/kodf scan /volume1/backups /volume1/to-sort
jbang kodf@koppor/kodf report
```

Without installing JBang, [gg.cmd](https://github.com/eirikb/gg) brings it along (one file for Linux, macOS and Windows):

```terminal
curl -L ggcmd.io > gg.cmd
sh ./gg.cmd jbang kodf@koppor/kodf scan /volume1/backups /volume1/to-sort
sh ./gg.cmd jbang kodf@koppor/kodf report
```

On first use, JBang asks whether to trust `https://github.com/koppor/kodf/`.

Example output:

```text
/volume1/backups/2019-01-05
  contained in /volume1/to-sort/whiteone
```

### What it does

- `scan DIR...` records every file below the given directories in `kodf.db`, an SQLite database (choose another file with `--db`).
  - Only files that share their size with another file are read and hashed (SHA-256); a file of unique size cannot have a duplicate.
    Of larger files, the first 64 KiB are compared first: a file differing there is not read fully.
  - A rescan reads only files whose size or modification time changed, and forgets files that are gone.
  - Empty files are not recorded.
  - `--min-size BYTES` leaves out smaller files, for a faster scan of photos and videos.
    A directory may then be reported although it holds smaller files that exist nowhere else.
  - Not scanned: `@eaDir`, `#recycle`, `#snapshot` (Synology), `$RECYCLE.BIN`, `.git`.
  - Scanning one directory after the other into the same database keeps the earlier results: `scan /volume1/photo`, later `scan /volume1/homes/koppor`, then `report` compares both.
    Rescanning a directory replaces only its own results.
  - On btrfs, a file sharing its data on disk with a file already hashed (a snapshot, a reflink copy, a deduped file) takes over that hash without being read.
  - Files and directories that cannot be read are reported on standard error; the directories around them are never reported as subsets, as their content is not fully known.
- `report` lists each directory whose whole content also exists in another directory, wherever that one lies, and names that directory.
  - A directory is not listed when a directory around it is listed already.
  - Only the innermost containing directories are named.
  - Two directories with the same content are both listed, each with the other.
  - On btrfs, a containing directory is marked `(already shares its data, deleting frees no space)` when the listed directory's data lies on disk only once.

- `report --fdupes` lists the groups of identical files instead, in the format of fdupes, leaving out groups that already share their data.
  [duperemove](https://github.com/markfasheh/duperemove) lets them share their data on btrfs or XFS without reading all files again: `kodf report --fdupes | duperemove --fdupes -d`.

Nothing is deleted or changed; `report` only lists.

### Where the database goes

`kodf.db` is created in the current directory, or wherever `--db` points.
It takes about 250 to 500 bytes per file, depending on the length of the paths: about 0.5 GB for one million files.
Files that share their size with another file take about 250 bytes more.
During a scan, SQLite's write-ahead log (`kodf.db-wal`) can temporarily grow to a similar size.
On a Synology NAS, keep it on a data volume (such as `--db /volume1/homes/<user>/kodf.db`), not on the small system partition, where `/root` lies.
Keep it outside the scanned directories, otherwise it is scanned as well.

### Permissions

kodf needs no root rights, only read access to what it scans.
Run it as your own user; if some directories are not readable for that user, the report skips the directories around them instead of giving a wrong answer.

### Planned

- A web page served by kodf that shows the directories as nested circles, and shared content as overlapping circles.

The planned scope is tracked in [docs/requirements](docs/requirements/README.md), the decisions in [docs/decisions](docs/decisions/README.md).

## Development

- Requires nothing but a JDK to start Gradle; Gradle downloads Java 27 itself.
- `./gradlew build` compiles, checks the formatting and runs the tests.
- `./test-on-btrfs.sh` runs the btrfs tests in a privileged Docker container on a btrfs image file; neither btrfs nor root is needed on the host.
- `./gradlew spotlessApply` formats the code with [Open Java Format](https://openjavaformat.dev/).
- `jbang kodf ...` runs the checkout, `jbang trace` traces requirements and decisions with [OpenFastTrace](https://github.com/itsallcode/openfasttrace).
- The Java version and the dependencies are declared in the JBang header of `App.java`; the Gradle build reads them from there.
