---
status: accepted
date: 2026-11-22
decision-makers: Oliver Kopp
---

# Scan on the NAS into an SQLite Database

`adr~sqlite-index~1`

Needs: dsn

## Context and Problem Statement

A NAS holds terabytes in millions of files.
Reading them over the network is slow, and reading them all again for each analysis is slower.
Where does the scanner run, and where does it keep what it found?

## Considered Options

* Scanner on the NAS, results in SQLite
* Scanner on a desktop over SMB, results in memory
* Scanner on the NAS, results in DuckDB

## Decision Outcome

Chosen option: "Scanner on the NAS, results in SQLite", because the disks are read locally, a rescan reads only changed files, and the viewer and any SQLite tool read the same single file.

### Consequences

* Good, because filesystem functions such as those of btrfs become usable, as they do not work over SMB.
* Bad, because kodf has to be installed on the NAS.

### Confirmation

The design items covering this decision state how the database is used.

## Pros and Cons of the Options

### Scanner on a desktop over SMB, results in memory

* Good, because nothing is installed on the NAS.
* Bad, because every run reads all files over the network again.

### Scanner on the NAS, results in DuckDB

* Good, because it is fast for analytical queries.
* Bad, because the analysis does not need it, and few tools open its files.
