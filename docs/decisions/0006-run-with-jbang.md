---
status: accepted
date: 2026-11-22
decision-makers: Oliver Kopp
---

# Run with JBang

`adr~jbang~1`

Needs: impl

## Context and Problem Statement

kodf runs on a NAS, where neither a JDK nor a build tool is installed.
How is it started there?

## Considered Options

* JBang running the source from the repository
* Distribution built by Gradle, copied to the NAS
* GraalVM native image

## Decision Outcome

Chosen option: "JBang running the source from the repository", because `jbang kodf@koppor/kodf` downloads the Java runtime, the dependencies and the source of the default branch by itself.

The JBang header of `App.java` declares the Java version and the dependencies; the Gradle build reads them from there.

### Consequences

* Good, because installing JBang is the only step on the NAS.
* Good, because the version on `main` is run, the daily release.
* Bad, because Dependabot does not update the dependencies in the JBang header.

### Confirmation

The Gradle build compiles with the dependencies of the JBang header, so the header cannot become incomplete unnoticed.

## Pros and Cons of the Options

### Distribution built by Gradle, copied to the NAS

* Bad, because each update is copied by hand.

### GraalVM native image

* Good, because it starts fast and needs no Java runtime.
* Bad, because it is built per processor architecture of the NAS.
