# Kopp Duplicate Finder

Finds duplicate files in different directories.

- Inspired by [FastDuplicateFinder](https://github.com/carlbeech/fast-duplicate-finder)
- (Currently) more code-oriented
- Less UI than [dupeGuru](https://dupeguru.voltaicideas.net/)

Distinguishing feature:

- **Diretory subset identification**.
  Sometimes, a file-based backup is made to different places on the NAS.
  As user, I want to know if a directory can be completely erased.

## Development

- To format the code `./gradlew googleJavaFormat`
- We rely on [Project Lombok](https://projectlombok.org/)
- The might rely on Java 14 features

### Development Setup

- The project uses the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Configure IntelliJ's checkstyle plugin to use the build-in "Google Style"
- Install the [Lombok IntelliJ plugin](https://plugins.jetbrains.com/plugin/6317-lombok)
