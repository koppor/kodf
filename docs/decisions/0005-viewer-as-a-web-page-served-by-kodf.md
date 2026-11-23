---
status: accepted
date: 2026-11-22
decision-makers: Oliver Kopp
---

# Viewer as a Web Page Served by kodf

`adr~web-viewer~1`

## Context and Problem Statement

Which directory is contained in which is best seen as a picture: circles nested by size, and overlapping circles for directories sharing content (Euler diagrams).
How is that picture shown?

## Considered Options

* Web page served by kodf, drawn with d3
* JavaFX application

## Decision Outcome

Chosen option: "Web page served by kodf, drawn with d3", because d3 provides circle packing and zooming, Euler diagrams are available as layouts for it, and a browser anywhere in the home network can show the result of the scan on the NAS.

### Consequences

* Good, because nothing is installed on the desktop.
* Bad, because the page is written in JavaScript next to the Java code.

### Confirmation

The design items covering this decision state how the page is served and drawn.

## Pros and Cons of the Options

### JavaFX application

* Good, because everything is written in Java.
* Bad, because circle packing and Euler diagrams would be written by hand.
* Bad, because it reads the database over the network or a copy of it.
