#!/bin/bash

# check arguments
if [[ $# -ne 2 ]]; then
    echo "Usage: ./run-codec.sh <benchmark-file-path> <result-file-path>"
    exit 1
fi

# build Relish
ant jar

# run the benchmark
benchmark="$(basename $1)"
java -cp relish.jar:lib/javatuples-1.2.jar:lib/gson-2.8.0.jar app.codec.Main $benchmark $1 $2

