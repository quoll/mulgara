#!/bin/sh

# ==============================================================================
#
# Starts Mulgara. Expects the following environment/arguments:
#
#   OUTPUT_FILE       The file to append std out and std err to
#
# Note. All arguments are passed to the native shell
#
# ==============================================================================

if [ "$1" = "-o" ]; then
  output_file="$2"
  shift; shift
else
  output_file=/dev/null
fi

# Make current shell and child processes ignore SIGHUP and SIGINT
trap '' 1 2

"$@" < /dev/null >> "$output_file" 2>&1 &

exit 0
