---
status: accepted
date: 2026-11-26
decision-makers: Oliver Kopp
---

# JavaScript Libraries as WebJars

`adr~webjars~1`

Needs: dsn

## Context and Problem Statement

The viewer page uses d3.
The NAS and the browser may have no internet access, so the page must not load it from a CDN.
How does d3 get into kodf?

## Considered Options

* WebJar as a Maven dependency
* npm with a bundler
* Copy of `d3.min.js` in the repository
* CDN

## Decision Outcome

Chosen option: "WebJar as a Maven dependency", because JBang and Gradle resolve it like every other dependency, declared once in `//DEPS`, and no JavaScript tool chain is needed.

### Consequences

* Good, because `jbang kodf@koppor/kodf` serves the page without a build step.
* Bad, because the d3 WebJar pulls in the WebJars of all d3 modules, although only its bundled `dist/d3.min.js` is used.

### Confirmation

`ServeTest` requests `d3.min.js` from the running server.

## Pros and Cons of the Options

### npm with a bundler

* Good, because it is the usual way for web front ends.
* Bad, because building kodf then needs Node.js, and JBang cannot run the checkout as it is.

### Copy of `d3.min.js` in the repository

* Good, because it needs nothing at all.
* Bad, because updates are copied by hand and the repository holds a minified file.

### CDN

* Good, because nothing is shipped.
* Bad, because the page does not work without internet access.
