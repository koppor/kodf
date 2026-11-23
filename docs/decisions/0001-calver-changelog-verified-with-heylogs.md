---
status: accepted
date: 2026-11-22
decision-makers: Oliver Kopp
---

# CalVer Changelog, Verified with heylogs

`adr~calver-changelog~1`

Needs: impl

## Context and Problem Statement

Changes are pushed continuously to `experimental`; there is no release planning.
How are versions numbered, and how is the change history kept?

## Considered Options

* Keep a Changelog with CalVer day releases, checked by heylogs
* Keep a Changelog with SemVer releases

## Decision Outcome

Chosen option: "Keep a Changelog with CalVer day releases, checked by heylogs", because kodf has no API whose compatibility a semantic version could express, a date is a version number that needs no decision, and [heylogs](https://github.com/nbbrd/heylogs) turns format errors into build failures.

A scheduled workflow merges `experimental` into `main` every morning.
If `CHANGELOG.md` has an `Unreleased` section, the workflow renames it to the date of the previous day, the day on which the changes were made, and tags `main` with `v<date>`.

### Consequences

* Good, because releasing needs no manual step.
* Bad, because at most one release per day is possible.

### Confirmation

The `changelog` job of the test workflow checks `CHANGELOG.md` with heylogs.
