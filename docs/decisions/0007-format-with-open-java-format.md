---
status: accepted
date: 2026-11-23
decision-makers: Oliver Kopp
---

# Format with Open Java Format

`adr~open-java-format~1`

Needs: impl

## Context and Problem Statement

The code is formatted automatically and the build fails on unformatted code.
`App.java` starts with the JBang header (`//JAVA`, `//DEPS`), which must stay exactly as written.
Which formatter is used?

## Considered Options

* Open Java Format through Spotless
* google-java-format through Spotless

## Decision Outcome

Chosen option: "Open Java Format through Spotless", because it leaves JBang header lines untouched, keeps lambdas on the line where they start, and uses 4 spaces and 120 columns, which read better than 2 spaces and 100 columns.

### Consequences

* Good, because the JBang header needs no exemption from formatting.
* Bad, because the Gradle daemon needs `--add-exports` flags for the javac parser (`gradle.properties`).

### Confirmation

`./gradlew build` runs `spotlessCheck`, which fails on code that Open Java Format would change.

## Pros and Cons of the Options

### google-java-format through Spotless

* Good, because it is the most widespread Java formatter.
* Bad, because it rewrites `//DEPS` to `// DEPS`, which breaks JBang, so the header had to be excluded with `spotless:off`.
