#!/usr/bin/env bash

# gen_thrift_file.sh language src_path_for_thrift_file dest_path_folder

# The client has to be built using Apache thrift. In order to build the client,
# thrift needs a thrift file.
#
# This file uses the template thrift file (that works for the entire project)
# and adds a line if it is needed for scala.
#
# Usage:
# ./gen_thrift_file.sh language src_path_for_thrift_file destination_folder_of_modified_file
#
# language:
# One of [python, scala, java] to generate the thrift file for
#
# src_path_for_thrift_file:
# Where the original thrift file (modeldb/thrift/ModelDB.thrift) is located.
#
# destination_folder_of_modified_file:
# Where to store the new thrift file, which may or may not be modified.

lang=$1
src_path=$2
dest_folder=$3
dest_path="$dest_folder/ModelStorage.thrift"

# Make sure all of the params exist
if [ -z $1 ] && [ -z $2 ] && [ -z $3 ]; then
    echo "Invalid arguments"
    exit 1
fi

# Make the destination folder if it doesn't exist
mkdir -p $dest_folder

# Copy the Thrift file into place
# If it's in scala, write the namespace first
cat $src_path > $dest_path
