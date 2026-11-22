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

Run it on the NAS itself, so it reads the disks directly instead of over the network:

```terminal
kodf scan /volume1/backups /volume1/to-sort
kodf report
```

`scan` records all files in `kodf.db` (SQLite, choose another file with `--db`).
A rescan reads only files whose size or modification time changed.
Only files that share their size with another file are hashed.
`report` lists the directories whose whole content also exists elsewhere.

## Development

- Requires nothing but a JDK to start Gradle; Gradle downloads Java 27 itself.
- `./gradlew build` compiles, checks the formatting and runs the tests.
- `./gradlew spotlessApply` formats the code ([Google Java Style](https://google.github.io/styleguide/javaguide.html)).
- `./gradlew installDist` creates `build/install/kodf/bin/kodf`; it needs Java 27 as `JAVA_HOME`.
