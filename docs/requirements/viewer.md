# Graphical view

Requirements and design for showing the scan results as a picture in the browser.

## Requirements

### Circles in the browser
`req~circle-view~1`

The user opens a web page served by kodf on the NAS, which shows the scanned directories as circles nested by size.
Directories whose whole content exists elsewhere stand out, and for each of them the user sees where its content lies as well.
The page works without internet access.

Covers:

* `feat~graphical-view~1`

Needs: dsn

## Design

### Web server in kodf
`dsn~serve~1`

`kodf serve` starts the HTTP server of the JDK, by default on `127.0.0.1:8080` (`--address`, `--port`).
It serves the page, d3 from its WebJar, and `data.json`: the directories with the size of their own files, and the subsets of `kodf report`.

Covers:

* `req~circle-view~1`
* `adr~web-viewer~1`
* `adr~webjars~1`

Needs: impl, utest

### Zoomable circle packing
`dsn~circle-packing~1`

The page lays out the directory tree with d3's circle packing, starting at the first directory with more than one subdirectory.
Directories reported as subsets are orange.
Clicking a circle zooms into it, names its subdirectories, and lists the directories containing its content, which are outlined in blue.

Covers:

* `req~circle-view~1`

Needs: impl
