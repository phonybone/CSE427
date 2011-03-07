#!/bin/sh

cd $HOME/random/coursework/CSE427/hw4/bin
faa_file=$1
echo faa_file is $faa_file
java ProfileHMM $faa_file 2>&1 $faa_file.out

