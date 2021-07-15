#!/bin/sh

VER=v0.2.0
DIR=gals-build/

if [ -z "$1" ]
then
    echo "No installation directory supplied..."
    exit 1
fi

rm -Rf $DIR         2> /dev/null
rm -f gals-$VER.zip 2> /dev/null

echo Downloading...
wget -nv https://github.com/fsantanna-no/gals/releases/download/$VER/gals-$VER.zip

# --show-progress --progress=bar:force

echo Unziping...
unzip gals-$VER.zip

echo Copying...
cp $DIR/* $1
rm -Rf $DIR 2> /dev/null
