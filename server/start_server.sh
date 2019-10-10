#!/usr/bin/env bash

../scripts/gen_thrift_file.sh java '../thrift/ModelStorage.thrift' './src/main/thrift/'

# check if thrift version specified and pass that on
if [ -n "$1" ]; then
    THRIFT_VERSION="-Dthrift_version=$1"
fi

# The next two lines of code must be seperate (not joined by &&)
# for Travis continuous integration tests to run
mvn clean compile $THRIFT_VERSION
mvn exec:java -Dexec.mainClass="main.Main" $THRIFT_VERSION &
