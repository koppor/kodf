#!/bin/sh
# Runs the btrfs tests in a privileged container, which mounts a btrfs image file: no btrfs and no root
# needed on the host, only Docker.
set -eu
exec docker run --rm --privileged \
  -v "$PWD":/src -v kodf-gradle:/root/.gradle -w /src \
  -e OWNER="$(id -u):$(id -g)" \
  eclipse-temurin:25 sh -c '
    trap "chown -R \$OWNER build .gradle" EXIT
    export DEBIAN_FRONTEND=noninteractive
    apt-get -qq update && apt-get -qq install -y btrfs-progs >/dev/null
    truncate -s 300M /btrfs.img && mkfs.btrfs -q /btrfs.img
    mkdir /btrfs && mount -o loop /btrfs.img /btrfs
    KODF_BTRFS_DIR=/btrfs ./gradlew --no-daemon --quiet test --tests "*BtrfsTest" "$@"
  ' sh "$@"
