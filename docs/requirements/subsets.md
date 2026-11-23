# Directory subsets

Requirements and design for scanning directories and reporting those whose content exists elsewhere.

## Requirements

### Scan on the NAS
`req~scan~1`

The user scans one or more directories on the NAS.
A repeated scan of the same directories is much faster than the first one when few files changed.

Covers:

* `feat~directory-subsets~1`

Needs: dsn

### Report of directory subsets
`req~subset-report~1`

The user gets a list of the directories whose whole content also exists in another directory, together with that directory.
The list is short enough to be read: a directory is not listed when a directory around it is listed already.

Covers:

* `feat~directory-subsets~1`

Needs: dsn

### Leave out small files
`req~min-size~1`

The user can leave out files below a size, so that a scan of a large collection of photos and videos finishes sooner.

Covers:

* `feat~directory-subsets~1`

Needs: dsn

## Design

### Results in an SQLite database
`dsn~sqlite-index~1`

`kodf scan` writes one row per regular file into the table `file` of an SQLite database: path, directory, size, modification time, and the SHA-256 hash of the content once it is known.
The database is a file chosen with `--db`, `kodf.db` by default.

Covers:

* `req~scan~1`
* `adr~sqlite-index~1`

Needs: impl, utest

### Rescan reads only changed files
`dsn~incremental-rescan~1`

A file keeps its known hash when its size and modification time are unchanged.
Files no longer found below a scanned directory are removed from the database.

Covers:

* `req~scan~1`

Needs: impl, utest

### Hash only files sharing their size
`dsn~hash-size-collisions~1`

Only files whose size is shared with another file are read and hashed, as a file of unique size cannot have a duplicate.
A directory holding a file without hash therefore cannot be a subset.
Empty files are not recorded: they are all equal and say nothing about where data is duplicated.

Covers:

* `req~scan~1`

Needs: impl, utest

### Ignored directories
`dsn~ignored-directories~1`

Directories named `@eaDir`, `#recycle`, `#snapshot` (Synology metadata, recycle bin and snapshots), `$RECYCLE.BIN` and `.git` are not scanned.

Covers:

* `req~scan~1`

Needs: impl

### Subsets anywhere in the scanned trees
`dsn~subset-anywhere~1`

The content of a directory is the set of hashes of all files below it.
A directory is a subset of another one when its content is contained in the content of the other one.
Ancestors and descendants of a directory are not compared with it, as deleting one of them would delete the other.

Covers:

* `req~subset-report~1`

Needs: impl, utest

### Outermost subset, smallest superset
`dsn~outermost-subset-smallest-superset~1`

`kodf report` lists a directory only when none of its ancestors is listed.
For each listed directory it names the directories containing its content of which no descendant contains it as well.
Two directories with the same content are both listed, each with the other.

Covers:

* `req~subset-report~1`

Needs: impl, utest

### Report on the command line
`dsn~report~1`

`kodf report` prints each listed directory, followed by one line `contained in <directory>` per containing directory.

Covers:

* `req~subset-report~1`

Needs: impl

### Minimum file size
`dsn~min-size~1`

`kodf scan --min-size <bytes>` does not record files below that size.
A directory may then be reported as a subset although it holds smaller files that exist nowhere else.

Covers:

* `req~min-size~1`

Needs: impl, utest
