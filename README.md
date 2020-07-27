# Koppor's Duplicate Finder

Finds duplicate files in different directories.

- Inspired by [FastDuplicateFinder](https://github.com/carlbeech/fast-duplicate-finder)
- (Currently) more code-oriented
- Less UI than [dupeGuru](https://dupeguru.voltaicideas.net/)

Distinguishing feature:

- **Diretory subset identification**.
  Sometimes, a file-based backup is made to different places on the NAS.
  As user, I want to know if a directory can be completely erased.
- **Arbitrary duplicate identification**.
  Finds duplicate directories in aribtrary sub folders.
  For instance, if one copy of the directory is contained in both `H:\backups\2019-01-05\data-from-white-hdd` and in `H:\0-to-sort\whiteone`, it is found
  With other tools, one has to start the comparison from `H:\backups\2019-01-05` and `H:\0-to-sort`, but who knows that `2019-01-05` is the right top level diretory to start from.

## Development

- To format the code `./gradlew googleJavaFormat`
- We rely on [Project Lombok](https://projectlombok.org/)
- Create an executable by invoking `gradlew runtime`. It will generate the `build/image` directory.
- Build linux image on Windows

   ```terminal
   docker run --rm -it -w /tmp/kodf -v c:\git-repositories\kodf:/tmp/kodf openjdk:14-jdk ./gradlew runtime
   ```

### Development Setup

- The project uses the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Configure IntelliJ's checkstyle plugin to use the build-in "Google Style"
- Install the [Lombok IntelliJ plugin](https://plugins.jetbrains.com/plugin/6317-lombok)
