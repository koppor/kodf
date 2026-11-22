# Decisions

Architectural decisions are recorded as [Markdown Architectural Decision Records](https://adr.github.io/madr/) ([decision 0000](0000-use-markdown-architectural-decision-records.md)).

| Record | Decision |
| --- | --- |
| [0000](0000-use-markdown-architectural-decision-records.md) | Use Markdown Architectural Decision Records |
| [0001](0001-calver-changelog-verified-with-heylogs.md) | CalVer changelog, verified with heylogs |
| [0002](0002-requirements-tracing-with-openfasttrace.md) | Requirements tracing with OpenFastTrace, run by JBang |
| [0003](0003-java-in-its-latest-release.md) | Java in its latest release |
| [0004](0004-scan-on-the-nas-into-sqlite.md) | Scan on the NAS into an SQLite database |
| [0005](0005-viewer-as-a-web-page-served-by-kodf.md) | Viewer as a web page served by kodf |
| [0006](0006-run-with-jbang.md) | Run with JBang |

## Conventions

* File name: `NNNN-title-in-kebab-case.md`, numbered consecutively.
* A record is not rewritten after it is accepted.
  A later decision supersedes it explicitly and sets the old record's status to `superseded by NNNN`.
* Each record defines one OpenFastTrace item `adr~<slug>~1` directly below its title, see [requirements](../requirements/README.md#decisions-in-the-trace).
