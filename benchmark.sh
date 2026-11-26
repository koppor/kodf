#!/bin/sh
# Times fdupes and kodf on the same directory: ./benchmark.sh DIR [DB]
# Run it as root to drop the page cache before each run; otherwise a later run reads from memory and
# looks faster than the disk allows. DB (default ./kodf-benchmark.db) must lie outside DIR; it is replaced.
set -eu
[ $# -ge 1 ] || { echo "usage: $0 DIR [DB]" >&2; exit 2; }
dir=$1
db=${2:-./kodf-benchmark.db}
kodf=${KODF:-jbang kodf@koppor/kodf}
command -v fdupes >/dev/null || { echo "fdupes not found; on Synology: opkg install fdupes (Entware)" >&2; exit 1; }

run() {
  label=$1
  shift
  if [ "$(id -u)" = 0 ]; then
    sync
    echo 3 > /proc/sys/vm/drop_caches
  fi
  start=$(date +%s)
  "$@" > /dev/null
  echo "$label: $(($(date +%s) - start)) s"
}

[ "$(id -u)" = 0 ] || echo "Not root: the page cache is not dropped, later runs profit from earlier ones." >&2
rm -f "$db" "$db-wal" "$db-shm"
# JBang compiles kodf on first use; that is not part of the comparison.
$kodf --help > /dev/null

run "fdupes -r" fdupes -r -q "$dir"
run "kodf scan" $kodf --db "$db" scan "$dir"
run "kodf report" $kodf --db "$db" report
run "kodf scan, unchanged files" $kodf --db "$db" scan "$dir"
echo "kodf database: $(du -h "$db" | cut -f1)"
