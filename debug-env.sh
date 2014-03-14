#!/bin/sh
basedir=`pwd`
srcs=`find $basedir -type d | egrep 'src/main/resources/static/rapture$' | sed 's/static\/rapture//'`

dirs=""
for s in $srcs; do
    if [ -n "$dirs" ]; then
        dirs="${dirs},"
    fi
    dirs="${dirs}$s"
done

export NEXUS_RESOURCE_DIRS="$dirs"