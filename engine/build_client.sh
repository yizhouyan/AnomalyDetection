#!/usr/bin/env bash
../scripts/gen_thrift_file.sh scala '../thrift/ModelStorage.thrift' './src/main/thrift/'
sbt clean && sbt assembly
