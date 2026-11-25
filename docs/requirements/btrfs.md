# Use of btrfs

Requirements and design for recognizing files that already share their data on disk.

## Requirements

### Directories that already share their data
`req~shared-data~1`

For a listed directory, the user learns whether its data already shares the disk space with the containing directory, as deleting it then frees no space.

Covers:

* `feat~btrfs~1`

Needs: dsn

### Shared data is not read
`req~shared-data-not-read~1`

A file sharing its data with a file read before is not read again, so that a scan of snapshots or reflink copies finishes sooner.

Covers:

* `feat~btrfs~1`

Needs: dsn

### Duplicates for duperemove
`req~duperemove~1`

The user lets duperemove dedupe the duplicates kodf found, without duperemove reading all files again.

Covers:

* `feat~btrfs~1`

Needs: dsn

## Design

### Fingerprint of the location on disk
`dsn~extent-fingerprint~1`

For each file that is hashed, `kodf scan` records in the column `extents` a SHA-256 over the logical offset, physical address and length of each extent, read with the FIEMAP ioctl of Linux.
Files with the same fingerprint share their data.
The fingerprint is left empty when FIEMAP is not available, or when an extent is compressed, inline, encrypted or not yet written, as its physical address then does not tell which bytes it holds.
On other filesystems than btrfs no two files share an extent, so no two fingerprints are equal.

Covers:

* `req~shared-data~1`
* `req~shared-data-not-read~1`

Needs: impl, utest

### Hash taken over from a file sharing the data
`dsn~shared-data-not-read~1`

A file with the same size and fingerprint as a file with known hash gets that hash without being read.

Covers:

* `req~shared-data-not-read~1`

Needs: impl, utest

### Report of shared data
`dsn~shared-data-report~1`

A listed directory shares its data with a containing directory when each file below it has a fingerprint that a file below the containing directory has as well.

Covers:

* `req~shared-data~1`

Needs: impl, utest

### Groups of identical files in the format of fdupes
`dsn~fdupes-groups~1`

`kodf report --fdupes` prints each group of files with the same hash, one path per line and a blank line after each group, as `fdupes` does and `duperemove --fdupes` reads.
A group is left out when all its files have the same fingerprint, as they already share their data.
duperemove compares the bytes itself before it lets files share their data, so a file changed since the scan is not harmed.

Covers:

* `req~duperemove~1`

Needs: impl, utest
