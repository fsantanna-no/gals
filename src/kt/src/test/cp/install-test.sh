#!/bin/sh

VER=test
DIR=gals-build/

if [ -z "$1" ]
then
    echo "No installation directory supplied..."
    exit 1
fi

echo Unziping...
unzip gals-$VER.zip

echo Copying...
cp $DIR/* $1
rm -Rf $DIR 2> /dev/null
