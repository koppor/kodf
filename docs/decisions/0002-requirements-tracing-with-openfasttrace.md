---
status: accepted
date: 2026-11-22
decision-makers: Oliver Kopp
---

# Requirements Tracing with OpenFastTrace, Run by JBang

`adr~openfasttrace~1`

Needs: impl

## Context and Problem Statement

The planned scope (viewer, btrfs support) is larger than what one development session implements.
How are requirements and decisions recorded and connected to the code?

## Considered Options

* OpenFastTrace with requirements and decisions in the trace, run by JBang
* OpenFastTrace through its Gradle plugin
* GitHub issues

## Decision Outcome

Chosen option: "OpenFastTrace with requirements and decisions in the trace, run by JBang", because the chain `feat → req → dsn → impl/utest` shows mechanically what is not yet designed, implemented or tested, and JBang runs the released OpenFastTrace jar without a build plugin.

Each decision record defines an item of the artifact type `adr`.
A decision about the product is covered by the `dsn` items that follow from it; a decision about the project infrastructure is covered by `impl` tags in build scripts and workflows.

### Consequences

* Good, because `jbang trace` fails on a requirement or decision without coverage, locally and in the continuous integration.
* Bad, because Dependabot does not update the OpenFastTrace version in `jbang-catalog.json`.

### Confirmation

The `trace` job of the test workflow runs `jbang trace`.

## Pros and Cons of the Options

### OpenFastTrace through its Gradle plugin

* Good, because Dependabot updates the plugin.
* Bad, because the plugin lags behind the OpenFastTrace releases.

### GitHub issues

* Bad, because issues are not versioned with the code and cannot be traced to it.
