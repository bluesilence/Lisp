#!/bin/sh
$filename="$1"
echo $filename
$output="$1_sorted.log"
sort -t ; -k 2 -o output -r $filename
