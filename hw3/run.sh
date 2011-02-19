#!/bin/sh

mkdir -p bin
cd bin
ln -s ../hcrs.ser .
ln -s ../*.filtered .
cd ..

ant build
cd bin
java AlignHCRs > ../alignment.txt
