# Features

The features describe the planned scope.
A feature without a `Needs:` line is not yet refined into requirements.

## Directory subsets
`feat~directory-subsets~1`

kodf tells which directories can be deleted without losing data, because their whole content also exists in another directory.
The other directory may lie anywhere in the scanned trees, under any name.

Needs: req

## Graphical view
`feat~graphical-view~1`

The directories are shown as circles nested by size.
Directories sharing content are shown as overlapping circles, a directory contained in another one as a circle inside it.

## Use of btrfs
`feat~btrfs~1`

On btrfs, kodf recognizes files that already share their data on disk without reading them.
Instead of deleting a duplicate, the user can let both files share their data, so that it takes up disk space only once.
