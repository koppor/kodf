---
status: accepted
date: 2026-11-22
decision-makers: Oliver Kopp
---

# Java in Its Latest Release

`adr~java~1`

Needs: impl

## Context and Problem Statement

kodf reads whole disks of a NAS, stores what it found, and serves a graphical view of the result.
It may later call filesystem functions of Linux directly, such as those of btrfs.
In which language is it written?

## Considered Options

* Java in its latest release
* Rust
* Go
* Kotlin
* Python
* Dart

## Decision Outcome

Chosen option: "Java in its latest release", because the maintainer knows it best, the Foreign Function & Memory API calls Linux functions such as `ioctl` without C code, and JBang runs it without an installed JDK.
The work is bound by disk reads, so the speed of the language does not matter.

### Consequences

* Good, because the existing build and tooling knowledge (Gradle, JBang, OpenFastTrace) applies.
* Bad, because the NAS needs a Java runtime, which JBang downloads, or a native image.

### Confirmation

The Gradle build compiles with the toolchain declared in the JBang header of `App.java`.

## Pros and Cons of the Options

### Rust

* Good, because it yields one small binary for the NAS.
* Bad, because it is slower to write for this one-person project.

### Go

* Good, because it yields one static binary for the NAS.
* Bad, because it brings no advantage over Java for disk-bound work.

### Kotlin

* Good, because it runs on the same platform.
* Bad, because it adds a compiler without changing what can be built.

### Python

* Bad, because it is slow for the in-memory analysis and harder to ship to a NAS.

### Dart

* Bad, because its strength, Flutter, is not needed for a viewer in the browser.
